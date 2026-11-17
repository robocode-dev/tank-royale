package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.util.Platform
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Duration
import java.util.Comparator
import java.util.UUID
import java.util.zip.ZipInputStream

/** Why a session cannot run its Python bots. */
enum class SessionPythonProblem {
    /** No Python is found on the path, or it does not answer. */
    MISSING,

    /** The Python found is older than the Rumble supports. */
    TOO_OLD,

    /** Installing the Bot API failed. */
    INSTALL_FAILED,

    /** The program carries no Python Bot API, or the one it carries cannot be read. */
    NO_BOT_API,
}

/** How setting up Python for a session ended. */
sealed interface SessionPythonSetup {
    /** [python] is the absolute path of the contributor's Python, [version] its version. */
    data class Ready(val python: String, val version: String) : SessionPythonSetup

    /** [needed] is the version the Rumble supports when known, [found] the one here, [details] the last lines of output. */
    data class Unavailable(val problem: SessionPythonProblem, val needed: String? = null, val found: String? = null, val details: String = "") : SessionPythonSetup
}

/**
 * The program's own Python Bot API wheel. Its file name, which pip checks against the version inside it, and the version
 * bounds of what the Bot API imports when a bot runs both come from the wheel's own metadata, so they cannot drift from it.
 */
class PythonBotApiWheel(private val open: () -> InputStream) {
    private val metadata: String by lazy {
        open().use { input ->
            ZipInputStream(input).use { zip ->
                generateSequence { zip.nextEntry }.firstOrNull { it.name.endsWith(".dist-info/METADATA") }
                    ?: throw IOException("The Python Bot API wheel has no metadata.")
                zip.readBytes().toString(Charsets.UTF_8)
            }
        }
    }

    /** The wheel's version, as its metadata names it. */
    val version: String get() = field("Version").firstOrNull() ?: throw IOException("The Python Bot API wheel names no version.")

    /** The file name pip needs: the distribution, the wheel's own version and the tags. */
    val fileName: String get() = "robocode_tank_royale-$version-py3-none-any.whl"

    /**
     * The wheel's own requirements for what the Bot API imports when a bot runs, with their version bounds. The wheel
     * declares more (pip, setuptools, PyYAML, mypy) for building it, which would make every battle's copy many times larger.
     */
    val runtimeRequirements: List<String>
        get() = field("Requires-Dist").map { it.substringBefore(';').trim() }
            .filter { requirement -> requirementName(requirement) in SessionPython.RUNTIME_IMPORTS }

    /** The lowest Python the wheel runs on, as `major.minor` from its `Requires-Python`, or null when it names none. */
    val lowestPython: String?
        get() = field("Requires-Python").firstOrNull()?.let { Regex(""">=\s*(\d+\.\d+)""").find(it)?.groupValues?.get(1) }

    fun writeTo(target: Path) = open().use { Files.copy(it, target) }

    private fun field(name: String): List<String> =
        metadata.lineSequence().takeWhile { it.isNotEmpty() }.filter { it.startsWith("$name: ") }.map { it.removePrefix("$name: ").trim() }.toList()

    companion object {
        fun requirementName(requirement: String): String = Regex("""^[A-Za-z0-9_.-]+""").find(requirement)?.value?.lowercase()?.replace('_', '-').orEmpty()
    }
}

/**
 * The program's own Python Bot API for one contribution session, as the decision that each ranked battle with a Python bot
 * gets its own copy sets out. It is installed with the contributor's Python into a new folder of the session's own below
 * [root], its digest is held in memory only, and every battle gets a copy in its own folder, checked against that digest
 * first. A folder that no longer matches is installed again. Nothing of it is ever named to a bot but the copy.
 *
 * Each session's folder has a lock file held while the session lives, so a session never removes another's folder, and
 * folders left by a session that was ended without closing, such as by a third Ctrl+C, are removed by the next one.
 */
class SessionPython(
    private val runner: ToolRunner,
    root: Path,
    private val wheel: PythonBotApiWheel?,
    /** The Python version the Rumble supports, such as `3.14`, or null when it is not known. */
    private val needed: String?,
    private val windows: Boolean = Platform.isWindows,
) : AutoCloseable {
    private val sessions = root.resolve(SESSIONS_FOLDER)
    private val id = UUID.randomUUID().toString()
    private val own = sessions.resolve(id)
    private var lockChannel: FileChannel? = null
    private var lock: FileLock? = null
    private var folder: Path? = null
    private var digest: String? = null
    private var python: String? = null

    /** Checks the Python on the path and installs the Bot API for the session. */
    @Synchronized
    fun setUp(): SessionPythonSetup {
        val wheel = wheel ?: return SessionPythonSetup.Unavailable(SessionPythonProblem.NO_BOT_API)
        // The Rumble's version when it is known; otherwise the lowest the wheel itself runs on, so an old Python is never taken.
        val needed = try {
            wheel.fileName
            needed ?: wheel.lowestPython
        } catch (e: IOException) {
            return SessionPythonSetup.Unavailable(SessionPythonProblem.NO_BOT_API, details = e.message.orEmpty())
        }
        val answer = runner.run(listOf(PythonEnvironment.systemPython(windows), "-c", "import sys; print(sys.version_info[0], sys.version_info[1], sys.executable)"), CHECK_TIMEOUT)
            ?.takeIf { it.exitCode == 0 }
            ?: return SessionPythonSetup.Unavailable(SessionPythonProblem.MISSING, needed)
        val parts = answer.text.trim().lines().lastOrNull().orEmpty().split(' ', limit = 3)
        val version = parts.take(2).mapNotNull { it.toIntOrNull() }
        if (version.size != 2 || parts.size < 3) return SessionPythonSetup.Unavailable(SessionPythonProblem.MISSING, needed)
        val found = version.joinToString(".")
        if (needed != null && !PlatformChecker.atLeast(version, needed)) return SessionPythonSetup.Unavailable(SessionPythonProblem.TOO_OLD, needed, found)
        python = parts[2].trim()
        try {
            // The lock is held before the folder exists, so no other session can ever find this folder unlocked.
            Files.createDirectories(sessions)
            lockChannel = FileChannel.open(sessions.resolve("$id.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            lock = lockChannel!!.lock()
            removeLeftovers()
            Files.createDirectories(own)
        } catch (e: IOException) {
            return SessionPythonSetup.Unavailable(SessionPythonProblem.INSTALL_FAILED, needed, found, e.message.orEmpty())
        }
        return install()?.let { SessionPythonSetup.Unavailable(SessionPythonProblem.INSTALL_FAILED, needed, found, it) }
            ?: SessionPythonSetup.Ready(python!!, found)
    }

    /**
     * Copies the session's Bot API into [battleFolder], after checking it against the digest in memory and installing it
     * again when it no longer matches, and gives the variables a battle with a Python bot needs. Throws [IOException]
     * with the reason when there is no usable Bot API.
     */
    @Synchronized
    fun forBattle(battleFolder: Path): Map<String, String> {
        val python = python ?: throw IOException("Python was not set up for this session.")
        try {
            val source = folder ?: throw IOException("Python was not set up for this session.")
            val copy = battleFolder.resolve(COPY_FOLDER)
            copyTree(source, copy)
            // The copy is what the battle runs, so checking it also checks the folder it came from. When it does not match,
            // the session's folder has changed: it is installed again and copied once more.
            if (digestOf(copy) != digest) {
                removeTree(copy)
                install()?.let { throw IOException("The Python Bot API changed and could not be installed again: $it") }
                copyTree(folder!!, copy)
                if (digestOf(copy) != digest) throw IOException("The copy of the Python Bot API does not match the session's.")
            }
            return mapOf(
                "PYTHONPATH" to copy.toString(),
                "PYTHONNOUSERSITE" to "1",
                "PYTHONDONTWRITEBYTECODE" to "1",
                "RUMBLE_PYTHON" to python,
            )
        } catch (e: UncheckedIOException) {
            throw IOException("The Python Bot API could not be read or copied: ${e.cause?.message ?: e.message}", e)
        }
    }

    /** Removes the session's folder and lets go of its lock. A folder that cannot be removed now is removed by a later session. */
    @Synchronized
    override fun close() {
        folder = null
        digest = null
        try {
            lock?.release()
            lockChannel?.close()
        } catch (e: IOException) {
            // The lock goes with the program anyway.
        }
        lock = null
        lockChannel = null
        removeTree(own)
        try {
            Files.deleteIfExists(sessions.resolve("$id.lock"))
        } catch (e: IOException) {
            // A later session removes it.
        }
    }

    /** Installs the Bot API into a new folder of the session's own. Null when it worked, or why not. */
    private fun install(): String? {
        val python = python ?: return "Python was not set up."
        val wheel = wheel ?: return "The program carries no Python Bot API."
        folder?.let { removeTree(it) }
        folder = null
        digest = null
        val target = own.resolve("api-${UUID.randomUUID()}")
        val wheelFolder = own.resolve("wheel-${UUID.randomUUID()}")
        try {
            Files.createDirectories(target)
            val file = Files.createDirectories(wheelFolder).resolve(wheel.fileName)
            wheel.writeTo(file)
            for (command in installCommands(python, target, file, wheel.runtimeRequirements)) {
                val output = runner.run(command, INSTALL_TIMEOUT)
                if (output == null || output.exitCode != 0) {
                    removeTree(target)
                    return output?.text?.trim()?.lines()?.takeLast(DETAIL_LINES)?.joinToString("\n") ?: "pip did not answer."
                }
            }
            digest = digestOf(target)
            folder = target
            return null
        } catch (e: IOException) {
            removeTree(target)
            return e.message ?: e.javaClass.simpleName
        } catch (e: UncheckedIOException) {
            removeTree(target)
            return e.cause?.message ?: e.message ?: e.javaClass.simpleName
        } finally {
            removeTree(wheelFolder)
        }
    }

    /**
     * Removes the folders of sessions that ended without closing: those whose lock file is there and held by nobody. A
     * folder whose lock file is gone is left alone, and another session's lock file is never removed, so a session that is
     * just starting can never lose its folder; the small lock files of ended sessions stay.
     */
    private fun removeLeftovers() {
        if (!Files.isDirectory(sessions)) return
        val folders = Files.list(sessions).use { paths -> paths.iterator().asSequence().filter { Files.isDirectory(it) && it != own }.toList() }
        for (other in folders) {
            val lockFile = sessions.resolve("${other.fileName}.lock")
            if (!Files.isRegularFile(lockFile)) continue
            val free = try {
                FileChannel.open(lockFile, StandardOpenOption.WRITE).use { channel -> channel.tryLock()?.also { it.release() } != null }
            } catch (e: IOException) {
                false
            } catch (e: OverlappingFileLockException) {
                false
            }
            if (free) removeTree(other)
        }
    }

    companion object {
        /** The packages outside the standard library that the Python Bot API imports when a bot runs. */
        val RUNTIME_IMPORTS = setOf("websockets", "pycountry")

        const val COPY_FOLDER = "rumble-python"
        const val SESSIONS_FOLDER = "python-sessions"

        private val CHECK_TIMEOUT: Duration = Duration.ofSeconds(20)
        private val INSTALL_TIMEOUT: Duration = Duration.ofMinutes(10)
        private const val DETAIL_LINES = 5

        /** The commands that install the Bot API and what it needs at run time into [target], in order. */
        fun installCommands(python: String, target: Path, wheel: Path, requirements: List<String>): List<List<String>> {
            val pip = listOf(python, "-m", "pip", "install", "--target", target.toString(), "--no-compile", "--disable-pip-version-check", "--no-input")
            val api = pip + listOf("--no-deps", wheel.toString())
            // pip refuses an install with nothing to install, so a wheel that requires nothing at run time gets one command.
            return if (requirements.isEmpty()) listOf(api) else listOf(api, pip + requirements)
        }

        /**
         * A digest of every file's path and content below [folder], read file by file, so a folder of tens of megabytes is
         * never held in memory at once.
         */
        fun digestOf(folder: Path): String {
            val all = MessageDigest.getInstance("SHA-256")
            val files = Files.walk(folder).use { paths -> paths.iterator().asSequence().filter { Files.isRegularFile(it) }.map { folder.relativize(it).toString().replace('\\', '/') }.sorted().toList() }
            val buffer = ByteArray(64 * 1024)
            for (name in files) {
                val one = MessageDigest.getInstance("SHA-256")
                Files.newInputStream(folder.resolve(name)).use { input ->
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        one.update(buffer, 0, count)
                    }
                }
                all.update(name.toByteArray(Charsets.UTF_8))
                all.update(0)
                all.update(one.digest())
            }
            return all.digest().joinToString("") { "%02x".format(it) }
        }

        private fun copyTree(source: Path, target: Path) {
            Files.walk(source).use { paths ->
                paths.forEach { path ->
                    val to = target.resolve(source.relativize(path).toString())
                    if (Files.isDirectory(path)) Files.createDirectories(to) else Files.copy(path, to)
                }
            }
        }

        /** Removes [folder] and what is in it, as far as it can; what is held open now is left for a later session. */
        private fun removeTree(folder: Path) {
            if (!Files.exists(folder)) return
            try {
                Files.walk(folder).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { runCatching { Files.deleteIfExists(it) } } }
            } catch (e: IOException) {
                // Left for a later session.
            } catch (e: UncheckedIOException) {
                // Left for a later session.
            }
        }
    }
}
