package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.ProgramResult
import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.rumble.BotProgressReading
import dev.robocode.tankroyale.rumble.BotSending
import dev.robocode.tankroyale.rumble.BotSubmission
import dev.robocode.tankroyale.rumble.CatalogSource
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.GitHubCatalogSource
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.SubmissionProgress
import dev.robocode.tankroyale.rumble.GitHubAuthTransport
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.HttpGitHubAuthTransport
import dev.robocode.tankroyale.rumble.GitHubResultDataSource
import dev.robocode.tankroyale.rumble.GitHubRuntimeListSource
import dev.robocode.tankroyale.rumble.ProcessToolRunner
import dev.robocode.tankroyale.rumble.PythonBotApiWheel
import dev.robocode.tankroyale.rumble.RuntimeListSource
import dev.robocode.tankroyale.rumble.ToolRunner
import dev.robocode.tankroyale.rumble.ResultDataSource
import dev.robocode.tankroyale.rumble.WorkerCommand
import dev.robocode.tankroyale.rumble.WorkerCommands
import dev.robocode.tankroyale.rumble.WorkerLimits
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.PrintStream
import java.nio.file.Path
import java.time.Clock

/** The exit codes of the command line. */
object ExitCodes {
    const val DONE = 0
    const val FAILED = 1
    const val REFUSED = 2
    const val SIGN_IN_NEEDED = 3

    /** Not finished; run the same command again. */
    const val NOT_FINISHED = 4
}

/** The program's own Python Bot API wheel, carried as a resource, as a writer to a file; null when it is not carried. */
internal fun carriedPythonBotApi(): PythonBotApiWheel? {
    if (Services::class.java.getResource(PYTHON_BOT_API) == null) return null
    return PythonBotApiWheel { Services::class.java.getResourceAsStream(PYTHON_BOT_API)!! }
}

private const val PYTHON_BOT_API = "/robocode-tank-royale-bot-api-python.whl"

/** The heap of the battle worker, which runs the server and the battle; the bots are processes of their own. */
private const val WORKER_HEAP_MEGABYTES = 512

/**
 * Calls [action] at each of the first two Ctrl+C instead of ending the program, until the returned handle is closed; a
 * third Ctrl+C ends the program the usual way, so a person is never kept from ending it. Java gives no supported way to
 * catch Ctrl+C, so the signal handler of the JDK is used.
 *
 * When Ctrl+C is already ignored, as for a command started in the background by a shell, no Ctrl+C can come and nothing
 * is installed. Where the JDK's signal handler is missing, a shutdown hook calls [action] once and then holds the program
 * open until the handle is closed, at most [SHUTDOWN_WAIT], so the battle can finish and the answer be written; the
 * program then ends with the code of the signal, not the command's own.
 */
internal fun onInterrupt(action: () -> Unit): AutoCloseable =
    try {
        val signal = sun.misc.Signal("INT")
        val presses = java.util.concurrent.atomic.AtomicInteger()
        lateinit var before: sun.misc.SignalHandler
        before = sun.misc.Signal.handle(signal) {
            if (presses.incrementAndGet() >= 2) sun.misc.Signal.handle(signal, before)
            action()
        }
        AutoCloseable { sun.misc.Signal.handle(signal, before) }
    } catch (e: IllegalArgumentException) {
        // Ctrl+C is ignored for this program, so none can come.
        AutoCloseable { }
    } catch (e: LinkageError) {
        val closed = java.util.concurrent.CountDownLatch(1)
        val hook = Thread {
            action()
            closed.await(SHUTDOWN_WAIT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
        }
        Runtime.getRuntime().addShutdownHook(hook)
        AutoCloseable {
            closed.countDown()
            try {
                Runtime.getRuntime().removeShutdownHook(hook)
            } catch (e: IllegalStateException) {
                // The program is already ending, and the hook is what holds it open; it has just been let go.
            }
        }
    }

/** How long a program that is ending holds open for the session to finish its battle and answer. */
private val SHUTDOWN_WAIT: java.time.Duration = java.time.Duration.ofMinutes(35)

/** What a command answers: its exit code, the same facts as JSON and as text for a person. */
class Reply(val exitCode: Int, val json: JsonObject, val text: String)

/** Where the commands reach GitHub and tell the time. Tests give fakes; nothing is built until a command needs it. */
class Services(
    makeAuth: () -> GitHubAuthTransport = { HttpGitHubAuthTransport() },
    makeApi: () -> GitHubApi = { HttpGitHubApi() },
    val clock: Clock = Clock.systemUTC(),
    val sleep: (Long) -> Unit = { Thread.sleep(it) },
    val registrationPollSeconds: Int = 5,
    val registrationWaitSeconds: Int = 120,
    /** Reads an environment variable. */
    val readEnv: (String) -> String? = { System.getenv(it) },
    /** Where the bot repository's public catalog and rules are read. */
    val catalog: (RumbleEnvironment) -> CatalogSource = { GitHubCatalogSource(it) },
    /** Sends a bot; given the GitHub API, so a test can stand in for the whole of sending. */
    private val makeSubmission: ((RumbleEnvironment, GitHubApi) -> BotSending)? = null,
    /** Reads the progress of a sent bot. */
    private val makeProgress: ((RumbleEnvironment, GitHubApi) -> BotProgressReading)? = null,
    /** Where the result repository's engine pin and advice are read. */
    val resultData: (RumbleEnvironment) -> ResultDataSource = { GitHubResultDataSource(it) },
    /** How a session starts the battle worker, given the environment's state folder: the carried worker by default. */
    val worker: (Path) -> WorkerCommand = { stateRoot -> WorkerCommands.javaJar(EmbeddedWorker.extract(stateRoot), WORKER_HEAP_MEGABYTES, ownJava()) },
    /** The limits of one battle's worker. */
    val workerLimits: WorkerLimits = WorkerLimits(totalTime = java.time.Duration.ofMinutes(30)),
    /** Calls the given action at each Ctrl+C for as long as the returned handle is open. */
    val interrupts: (() -> Unit) -> AutoCloseable = ::onInterrupt,
    /** The seed of a session's choices among equals. */
    val seed: () -> Long = { System.nanoTime() },
    /** Finds a program on the path as a bot's launcher does, giving its full path, or null when it is not there. */
    val locate: (String) -> String? = { dev.robocode.tankroyale.rumble.PlatformChecker.onPath(it, dev.robocode.tankroyale.common.util.Platform.isWindows) },
    /** Runs the tools a session needs on this computer, such as Python and pip. */
    val tools: ToolRunner = ProcessToolRunner(),
    /** The Rumble's list of supported runtime versions. */
    val runtimeList: (GitHubApi) -> RuntimeListSource = { GitHubRuntimeListSource(it) },
    /** The program's own Python Bot API wheel; null when the program carries none. */
    val pythonBotApi: PythonBotApiWheel? = carriedPythonBotApi(),
) {
    fun submission(env: RumbleEnvironment): BotSending = (makeSubmission ?: { e, a -> BotSubmission(e, a) })(env, api)

    fun progress(env: RumbleEnvironment): BotProgressReading = (makeProgress ?: { e, a -> SubmissionProgress(e, a) })(env, api)

    private val realAuth by lazy(makeAuth)
    private val realApi by lazy(makeApi)

    /** Reaches GitHub's sign-in pages; the connection is built at the first request. */
    val auth: GitHubAuthTransport = object : GitHubAuthTransport {
        override fun postForm(url: String, form: Map<String, String>) = realAuth.postForm(url, form)
    }

    /** Reaches GitHub's API; the connection is built at the first request. */
    val api: GitHubApi = object : GitHubApi {
        override fun get(path: String, token: String?) = realApi.get(path, token)
        override fun postJson(path: String, token: String, json: String) = realApi.postJson(path, token, json)
        override fun patchJson(path: String, token: String, json: String) = realApi.patchJson(path, token, json)
    }
}

/**
 * Removes what must not be in an answer that may be pasted into an issue or a chat. Every answer loses the home folder
 * and any absolute path of this computer, which never touches the program's own words. The user name is removed only
 * from text that comes from outside the program, because it can be any word: a user called `test` must not change the
 * name of an environment, nor one called `anna` the sign-in code `ANNA-1234`.
 */
class Redaction(private val home: String, private val user: String) {
    /** [text] without the home folder and any absolute path. */
    fun paths(text: String): String {
        var result = text
        for (form in setOf(home, home.replace("\\", "/"), home.replace("/", "\\"), home.replace("\\", "\\\\")).filter { it.length > 3 }.sortedByDescending { it.length }) {
            result = result.replace(form, "~")
        }
        result = WINDOWS_PATH.replace(result, "<path>")
        return UNIX_HOME_PATH.replace(result, "<path>")
    }

    /**
     * Text from outside the program, without paths and the user name. When the user name is also [publicName], the
     * participant's public GitHub account name, it stays: it is no secret, and the text would otherwise be wrong.
     */
    fun outside(text: String, publicName: String? = null): String {
        val result = paths(text)
        if (user.length < 3 || user.equals(publicName, ignoreCase = true)) return result
        return Regex("(?i)(?<![\\p{L}\\p{N}_-])" + Regex.escape(user) + "(?![\\p{L}\\p{N}_-])").replace(result, "<user>")
    }

    companion object {
        // The drive letter must stand alone, so the end of a scheme in a web address, like the s in https://, is no path.
        private val WINDOWS_PATH = Regex("(?<![A-Za-z0-9])[A-Za-z]:[\\\\/][^\\s\"'<>|]*")
        private val UNIX_HOME_PATH = Regex("/(?:home|Users)/[^\\s\"'<>:]+")

        fun forThisComputer() = Redaction(System.getProperty("user.home").orEmpty(), System.getProperty("user.name").orEmpty())
    }
}

/** Where a command writes and where state lives by default. Tests give it their own streams and folders. */
class Io(
    val out: PrintStream,
    val err: PrintStream,
    val defaultStateFolder: () -> Path = { UserDataDirectory.get().toPath() },
    private val redaction: Redaction = Redaction.forThisComputer(),
) {
    /** Writes [reply] as text or as one JSON document, cleaned, and ends the command with its exit code. */
    fun answer(reply: Reply, asJson: Boolean) {
        out.println(if (asJson) encoded(reply.json) else redaction.paths(reply.text))
        out.flush()
        if (reply.exitCode != ExitCodes.DONE) throw ProgramResult(reply.exitCode)
    }

    /** Writes [reply] as text, cleaned, and carries on. Only for a command in text form that has more to say. */
    fun say(reply: Reply) = say(reply.text)

    /** Writes [text], cleaned, and carries on. Only for a command in text form that has more to say. */
    fun say(text: String) {
        out.println(redaction.paths(text))
        out.flush()
    }

    /** Writes one event of a running command as one cleaned JSON line, and carries on. */
    fun event(json: JsonObject) {
        out.println(encoded(json))
        out.flush()
    }

    /** Writes an answer no command gave, such as to bad input, as one cleaned JSON document. */
    fun answerProblem(json: JsonObject) {
        out.println(encoded(json))
        out.flush()
    }

    /** [text] without paths. */
    fun cleaned(text: String): String = redaction.paths(text)

    /** Text from outside the program, cleaned for use in an answer. See [Redaction.outside]. */
    fun outside(text: String, publicName: String? = null): String = redaction.outside(text, publicName)

    /** One JSON document with every text in it cleaned first, so the cleaning can never damage the document itself. */
    private fun encoded(json: JsonObject): String = Json.encodeToString(JsonElement.serializer(), cleanedValues(json))

    private fun cleanedValues(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject(element.mapValues { cleanedValues(it.value) })
        is JsonArray -> JsonArray(element.map { cleanedValues(it) })
        is JsonNull -> element
        is JsonPrimitive -> if (element.isString) JsonPrimitive(redaction.paths(element.content)) else element
    }
}

/**
 * The Java this program runs on, by its full path: the worker runs on it, so a session needs no Java on the path for
 * its battles, also when the program was started from the desktop program's own Java. On Windows it is the variant
 * without a console window.
 */
internal fun ownJava(): String {
    val bin = java.nio.file.Path.of(System.getProperty("java.home"), "bin")
    val windows = dev.robocode.tankroyale.common.util.Platform.isWindows
    val quiet = bin.resolve(if (windows) "javaw.exe" else "java")
    return (if (java.nio.file.Files.isRegularFile(quiet)) quiet else bin.resolve(if (windows) "java.exe" else "java")).toString()
}
