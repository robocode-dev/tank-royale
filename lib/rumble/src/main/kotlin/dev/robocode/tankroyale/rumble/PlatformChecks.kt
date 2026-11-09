package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.RUMBLE_PYTHON_ENVIRONMENT
import dev.robocode.tankroyale.common.util.Platform
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.time.Duration
import java.util.Base64
import java.util.Comparator
import java.util.Properties
import java.util.concurrent.TimeUnit

/** A platform of the catalog, named as the bot repository's rules name it. */
enum class BotPlatform(val catalogName: String) {
    JVM("JVM"), CSHARP("C#"), PYTHON("Python"), TYPESCRIPT("TypeScript");

    companion object {
        fun of(catalogName: String): BotPlatform? = entries.firstOrNull { it.catalogName == catalogName }
    }
}

/** Why a platform can or cannot run here. The texts are in the resources. */
enum class PlatformReason {
    READY,

    /** The tool is not found, or it does not answer. */
    MISSING,

    /** The tool is older than the Rumble supports. */
    TOO_OLD,

    /** The tool answered with something that is not a version. */
    UNREADABLE,

    /** Node.js is there, but npm is not. */
    NPM_MISSING,

    /** Python can run the bots once the download folder has its own Python environment. */
    NO_ENVIRONMENT,

    /** Python is there but cannot make an environment, as on Debian or Ubuntu without the python3-venv package. */
    NO_VENV,
}

/** The answer for one platform: [needed] is the version the Rumble supports and [found] the one here, when known. */
data class PlatformCheck(val platform: BotPlatform, val reason: PlatformReason, val needed: String?, val found: String?) {
    val ready: Boolean get() = reason == PlatformReason.READY
}

/**
 * The versions the Rumble supports, from its one list: the runtime file of the Rumble client's latest release, from
 * which its container image is built. A platform passes with at least the major version named, and Python with at
 * least the major and minor version.
 */
data class SupportedVersions(val java: Int, val dotnet: Int, val python: List<Int>, val node: Int) {
    fun neededFor(platform: BotPlatform): String = when (platform) {
        BotPlatform.JVM -> "$java"
        BotPlatform.CSHARP -> "$dotnet"
        BotPlatform.PYTHON -> python.joinToString(".")
        BotPlatform.TYPESCRIPT -> "$node"
    }

    companion object {
        /** Reads the runtime file, or refuses one without the four versions. */
        fun parse(text: String): SupportedVersions {
            val properties = Properties().apply { load(text.reader()) }
            fun major(name: String) = properties.getProperty(name)?.trim()?.toIntOrNull()
                ?: throw IOException("The Rumble's runtime list has no version for $name.")
            val python = properties.getProperty("python")?.trim()?.split('.')?.map { it.toIntOrNull() }
            if (python == null || python.size != 2 || python.any { it == null }) throw IOException("The Rumble's runtime list has no version for python.")
            return SupportedVersions(major("java"), major("dotnet"), python.map { it!! }, major("node"))
        }
    }
}

/** Where the Rumble's runtime list is read from. Every call may take a long time and may throw [IOException]. */
fun interface RuntimeListSource {
    fun text(): String
}

/** Reads the runtime file of the Rumble client's latest release on GitHub. It is public, so no sign-in is sent. */
class GitHubRuntimeListSource(private val api: GitHubApi) : RuntimeListSource {
    override fun text(): String {
        val repository = "/repos/${RumbleEnvironments.owner}/$CLIENT_REPOSITORY"
        val release = api.get("$repository/releases/latest", null)
        if (release.status != 200) throw IOException("GitHub gave an unexpected answer (HTTP ${release.status}) about the Rumble client's latest release.")
        val tag = field(release.body, "tag_name") ?: throw IOException("GitHub's answer about the Rumble client's latest release could not be understood.")
        val file = api.get("$repository/contents/$RUNTIME_FILE?ref=$tag", null)
        if (file.status != 200) throw IOException("GitHub gave an unexpected answer (HTTP ${file.status}) about the Rumble's runtime list.")
        val content = field(file.body, "content") ?: throw IOException("GitHub's answer about the Rumble's runtime list could not be understood.")
        return String(Base64.getMimeDecoder().decode(content), Charsets.UTF_8)
    }

    private fun field(json: String, name: String): String? =
        try {
            Json.parseToJsonElement(json).jsonObject[name]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }

    companion object {
        const val CLIENT_REPOSITORY = "rumble-client"
        const val RUNTIME_FILE = "src/main/resources/runtime-versions.properties"
    }
}

/** What a program printed and how it ended. */
data class ToolOutput(val exitCode: Int, val text: String)

/** Runs a program. Null when it cannot be started or does not end in time. */
fun interface ToolRunner {
    fun run(command: List<String>, timeout: Duration): ToolOutput?
}

/** Runs real programs, with their output and errors read together, and stops one that does not end in time. */
class ProcessToolRunner : ToolRunner {
    override fun run(command: List<String>, timeout: Duration): ToolOutput? {
        val process = try {
            ProcessBuilder(command).redirectErrorStream(true).redirectInput(ProcessBuilder.Redirect.PIPE).start()
        } catch (e: IOException) {
            return null
        }
        process.outputStream.close()
        val output = StringBuilder()
        val reader = Thread {
            try {
                process.inputStream.bufferedReader().use { output.append(it.readText()) }
            } catch (e: IOException) {
                // The process was stopped while it was being read.
            }
        }.apply { isDaemon = true; start() }
        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            process.descendants().forEach { it.destroyForcibly() }
            process.destroyForcibly()
            return null
        }
        reader.join(TimeUnit.SECONDS.toMillis(5))
        return ToolOutput(process.exitValue(), synchronized(output) { output.toString() })
    }
}

/** The private Python environment of a download folder, as the booter starts the bots in it. */
object PythonEnvironment {
    const val FOLDER = RUMBLE_PYTHON_ENVIRONMENT

    fun home(downloadFolder: Path): Path = downloadFolder.resolve(FOLDER)

    fun python(downloadFolder: Path, windows: Boolean): Path =
        home(downloadFolder).resolve(if (windows) "Scripts" else "bin").resolve(if (windows) "python.exe" else "python")

    /** The Python the launchers call when no environment is given. */
    fun systemPython(windows: Boolean): String = if (windows) "python" else "python3"
}

/**
 * Asks the tools of each platform for their version and compares it with the Rumble's list. Checking only asks: it
 * installs or changes nothing. Every call may take seconds and is never made on the screen's thread.
 */
class PlatformChecker(private val runner: ToolRunner, private val windows: Boolean = Platform.isWindows) {

    /** [versions] is null when the Rumble's list could not be read; then a platform passes when its tool is there. */
    fun check(platforms: Collection<BotPlatform>, versions: SupportedVersions?, downloadFolder: Path): Map<BotPlatform, PlatformCheck> =
        platforms.distinct().associateWith { platform ->
            val needed = versions?.neededFor(platform)
            when (platform) {
                BotPlatform.JVM -> major(platform, needed, listOf("java", "-version"), JAVA)
                BotPlatform.CSHARP -> dotnet(needed)
                BotPlatform.TYPESCRIPT -> node(needed)
                BotPlatform.PYTHON -> python(needed, downloadFolder)
            }
        }

    private fun ask(command: List<String>): ToolOutput? = runner.run(command, TIMEOUT)?.takeIf { it.exitCode == 0 }

    private fun major(platform: BotPlatform, needed: String?, command: List<String>, pattern: Regex): PlatformCheck {
        val output = ask(command) ?: return PlatformCheck(platform, PlatformReason.MISSING, needed, null)
        val found = pattern.find(output.text)?.let { versionOf(it) } ?: return PlatformCheck(platform, PlatformReason.UNREADABLE, needed, null)
        return judge(platform, needed, found)
    }

    private fun dotnet(needed: String?): PlatformCheck {
        val output = ask(listOf("dotnet", "--list-sdks")) ?: return PlatformCheck(BotPlatform.CSHARP, PlatformReason.MISSING, needed, null)
        val sdks = SDK.findAll(output.text).map { it.groupValues[1].toInt() }.toList()
        if (sdks.isEmpty()) return PlatformCheck(BotPlatform.CSHARP, PlatformReason.MISSING, needed, null)
        return judge(BotPlatform.CSHARP, needed, listOf(sdks.max()))
    }

    private fun node(needed: String?): PlatformCheck {
        val node = major(BotPlatform.TYPESCRIPT, needed, listOf("node", "--version"), NODE)
        if (!node.ready) return node
        val npm = ask(listOf(if (windows) "npm.cmd" else "npm", "--version"))
        if (npm == null || !NUMBER.containsMatchIn(npm.text)) return node.copy(reason = PlatformReason.NPM_MISSING)
        return node
    }

    private fun python(needed: String?, downloadFolder: Path): PlatformCheck {
        val own = PythonEnvironment.python(downloadFolder, windows)
        if (Files.exists(own, LinkOption.NOFOLLOW_LINKS) || Files.exists(own)) {
            val answer = ask(listOf(own.toString(), "-c", "import robocode_tank_royale, sys; print('Python %d.%d.%d' % sys.version_info[:3])"))
            val found = answer?.let { PYTHON.find(it.text) }?.let { versionOf(it) }
            if (found != null && (needed == null || atLeast(found, needed))) return PlatformCheck(BotPlatform.PYTHON, PlatformReason.READY, needed, found.joinToString("."))
        }
        val system = major(BotPlatform.PYTHON, needed, listOf(PythonEnvironment.systemPython(windows), "--version"), PYTHON)
        if (!system.ready) return system
        ask(listOf(PythonEnvironment.systemPython(windows), "-c", "import venv, ensurepip"))
            ?: return system.copy(reason = PlatformReason.NO_VENV)
        return system.copy(reason = PlatformReason.NO_ENVIRONMENT)
    }

    private fun judge(platform: BotPlatform, needed: String?, found: List<Int>): PlatformCheck {
        val reason = if (needed == null || atLeast(found, needed)) PlatformReason.READY else PlatformReason.TOO_OLD
        return PlatformCheck(platform, reason, needed, found.joinToString("."))
    }

    companion object {
        private val TIMEOUT: Duration = Duration.ofSeconds(20)
        private val JAVA = Regex("""version "(?:1\.)?(\d+)(?:\.(\d+))?""")
        private val NODE = Regex("""v(\d+)\.(\d+)""")
        private val PYTHON = Regex("""Python (\d+)\.(\d+)""")
        private val SDK = Regex("""(?m)^\s*(\d+)\.\d+\.\d+""")
        private val NUMBER = Regex("""\d+\.\d+""")

        private fun versionOf(match: MatchResult): List<Int> = match.groupValues.drop(1).filter { it.isNotEmpty() }.map { it.toInt() }

        /** True when [found] is at least [needed], compared part by part as far as [needed] goes. */
        fun atLeast(found: List<Int>, needed: String): Boolean {
            val wanted = needed.split('.').map { it.toInt() }
            wanted.forEachIndexed { i, part ->
                val have = found.getOrElse(i) { 0 }
                if (have != part) return have > part
            }
            return true
        }
    }
}

/** How setting up Python for a download folder ended. */
sealed interface PythonSetupResult {
    data object Done : PythonSetupResult

    data class Failed(val step: PythonSetupStep, val details: String) : PythonSetupResult
}

/** The steps of setting up Python, in order. */
enum class PythonSetupStep { MAKE_ENVIRONMENT, INSTALL_BOT_API }

/**
 * Sets up the private Python environment of a download folder: makes it with the Python the launchers call, then
 * installs the program's own Python Bot API into it with the environment's own `python -m pip`. Nothing outside the
 * environment's folder is created or changed, and no other Python is installed into. A failed setup leaves no
 * half-made environment behind.
 */
class PythonSetup(private val runner: ToolRunner, private val windows: Boolean = Platform.isWindows) {

    /** The commands, in order, so they can be shown before they run. */
    fun commands(downloadFolder: Path, botApi: Path): List<List<String>> = listOf(
        listOf(PythonEnvironment.systemPython(windows), "-m", "venv", PythonEnvironment.home(downloadFolder).toString()),
        listOf(PythonEnvironment.python(downloadFolder, windows).toString(), "-m", "pip", "install", botApi.toString()),
    )

    fun run(downloadFolder: Path, botApi: Path): PythonSetupResult {
        val home = PythonEnvironment.home(downloadFolder)
        // An environment that no longer starts is replaced as a whole.
        remove(home)
        Files.createDirectories(downloadFolder)
        val (make, install) = commands(downloadFolder, botApi)
        listOf(PythonSetupStep.MAKE_ENVIRONMENT to (make to MAKE_TIMEOUT), PythonSetupStep.INSTALL_BOT_API to (install to INSTALL_TIMEOUT))
            .forEach { (step, run) ->
                val output = runner.run(run.first, run.second)
                if (output == null || output.exitCode != 0) {
                    remove(home)
                    return PythonSetupResult.Failed(step, output?.text?.trim()?.lines()?.takeLast(DETAIL_LINES)?.joinToString("\n").orEmpty())
                }
            }
        return PythonSetupResult.Done
    }

    private fun remove(home: Path) {
        if (!Files.exists(home, LinkOption.NOFOLLOW_LINKS)) return
        Files.walk(home).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) } }
    }

    companion object {
        private val MAKE_TIMEOUT: Duration = Duration.ofMinutes(2)
        private val INSTALL_TIMEOUT: Duration = Duration.ofMinutes(10)
        private const val DETAIL_LINES = 5
    }
}

/** The official download page of each platform's tool. The screen opens no other address for them. */
object PlatformDownloads {
    private val PAGES = mapOf(
        BotPlatform.JVM to "https://adoptium.net/temurin/releases/",
        BotPlatform.CSHARP to "https://dotnet.microsoft.com/en-us/download",
        BotPlatform.PYTHON to "https://www.python.org/downloads/",
        BotPlatform.TYPESCRIPT to "https://nodejs.org/en/download",
    )

    fun page(platform: BotPlatform): String = PAGES.getValue(platform)

    fun isFixedPage(address: String): Boolean = address in PAGES.values
}
