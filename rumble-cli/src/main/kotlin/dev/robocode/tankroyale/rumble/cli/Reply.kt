package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.ProgramResult
import dev.robocode.tankroyale.common.util.UserDataDirectory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.PrintStream
import java.nio.file.Path

/** The exit codes of the command line. 3 is reserved for the commands that need a sign-in, which do not exist yet. */
object ExitCodes {
    const val DONE = 0
    const val FAILED = 1
    const val REFUSED = 2
    const val SIGN_IN_NEEDED = 3
}

/** What a command answers: its exit code, the same facts as JSON and as text for a person. */
class Reply(val exitCode: Int, val json: JsonObject, val text: String)

/**
 * Removes what must not be in an answer that may be pasted into an issue or a chat: the home folder, the user name,
 * and any absolute path of this computer.
 */
class Redaction(private val home: String, private val user: String) {
    fun clean(text: String): String {
        var result = text
        for (form in setOf(home, home.replace("\\", "/"), home.replace("/", "\\"), home.replace("\\", "\\\\")).filter { it.length > 3 }.sortedByDescending { it.length }) {
            result = result.replace(form, "~")
        }
        result = WINDOWS_PATH.replace(result, "<path>")
        result = UNIX_HOME_PATH.replace(result, "<path>")
        if (user.length >= 3) result = Regex("(?i)\\b" + Regex.escape(user) + "\\b").replace(result, "<user>")
        return result
    }

    companion object {
        private val WINDOWS_PATH = Regex("[A-Za-z]:[\\\\/][^\\s\"'<>|]*")
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
        val raw = if (asJson) Json.encodeToString(JsonObject.serializer(), reply.json) else reply.text
        out.println(redaction.clean(raw))
        out.flush()
        if (reply.exitCode != ExitCodes.DONE) throw ProgramResult(reply.exitCode)
    }
}
