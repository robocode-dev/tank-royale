package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.ProgramResult
import dev.robocode.tankroyale.common.util.UserDataDirectory
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.GitHubAuthTransport
import dev.robocode.tankroyale.rumble.HttpGitHubApi
import dev.robocode.tankroyale.rumble.HttpGitHubAuthTransport
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

/** What a command answers: its exit code, the same facts as JSON and as text for a person. */
class Reply(
    val exitCode: Int,
    val json: JsonObject,
    val text: String,
    /** Public values the cleaning must leave whole: the signed-in account name and web addresses. */
    val keep: Set<String> = emptySet(),
)

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
) {
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
 * Removes what must not be in an answer that may be pasted into an issue or a chat: the home folder, the user name,
 * and any absolute path of this computer.
 */
class Redaction(private val home: String, private val user: String) {
    fun clean(text: String, keep: Set<String> = emptySet()): String {
        var result = text
        for (form in setOf(home, home.replace("\\", "/"), home.replace("/", "\\"), home.replace("\\", "\\\\")).filter { it.length > 3 }.sortedByDescending { it.length }) {
            result = result.replace(form, "~")
        }
        result = WINDOWS_PATH.replace(result, "<path>")
        result = UNIX_HOME_PATH.replace(result, "<path>")
        // The values to keep are set aside only here, after the paths, so a path holding one of them is still removed.
        val kept = keep.filter { it.isNotEmpty() }.sortedByDescending { it.length }
        kept.forEachIndexed { index, value -> result = result.replace(value, "\u0000$index\u0000") }
        if (user.length >= 3) result = Regex("(?i)\\b" + Regex.escape(user) + "\\b").replace(result, "<user>")
        kept.forEachIndexed { index, value -> result = result.replace("\u0000$index\u0000", value) }
        return result
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
        out.println(if (asJson) encoded(reply.json, reply.keep) else redaction.clean(reply.text, reply.keep))
        out.flush()
        if (reply.exitCode != ExitCodes.DONE) throw ProgramResult(reply.exitCode)
    }

    /** Writes [reply] as text, cleaned, and carries on. Only for a command in text form that has more to say. */
    fun say(reply: Reply) {
        out.println(redaction.clean(reply.text, reply.keep))
        out.flush()
    }

    /** Writes an answer no command gave, such as to bad input, as one cleaned JSON document. */
    fun answerProblem(json: JsonObject) {
        out.println(encoded(json, emptySet()))
        out.flush()
    }

    /** One JSON document with every text in it cleaned first, so the cleaning can never damage the document itself. */
    private fun encoded(json: JsonObject, keep: Set<String>): String =
        Json.encodeToString(JsonElement.serializer(), cleanedValues(json, keep))

    private fun cleanedValues(element: JsonElement, keep: Set<String>): JsonElement = when (element) {
        // The program's own words are what a program reads and runs, so a user name like one of them cannot change them.
        is JsonObject -> JsonObject(element.mapValues { (key, value) -> if (key in PROGRAM_WORDS) value else cleanedValues(value, keep) })
        is JsonArray -> JsonArray(element.map { cleanedValues(it, keep) })
        is JsonNull -> element
        is JsonPrimitive -> if (element.isString) JsonPrimitive(redaction.clean(element.content, keep)) else element
    }

    /** [text] with what must not be shown removed. */
    fun cleaned(text: String): String = redaction.clean(text)

    /** Writes [text], cleaned, and carries on. Only for a command in text form that has more to say. */
    fun say(text: String) {
        out.println(redaction.clean(text))
        out.flush()
    }

    private companion object {
        val PROGRAM_WORDS = setOf("command", "environment", "outcome")
    }
}
