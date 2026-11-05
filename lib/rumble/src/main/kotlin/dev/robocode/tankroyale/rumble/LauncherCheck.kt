package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.file.Files
import java.util.Base64

/** Whether a bot's launcher scripts are the Rumble's starter launchers, as far as the program could look. */
sealed interface LauncherState {
    /** Both launchers equal the starter launchers, so the moderator has nothing to ask a person about. */
    data object Same : LauncherState

    /** These launcher files differ from the starter launchers, so a moderator must look at the bot. */
    data class Differs(val files: List<String>) : LauncherState

    /** The program could not look, for the plain reason in [reason]. This is not the same as [Same]. */
    data class NotChecked(val reason: String) : LauncherState
}

/**
 * Compares a staged bot's launcher scripts with the starter launchers in the Rumble's bot repository, the way the
 * repository's moderation does: the starter launcher of the bot's language with `StarterBot` replaced by the bot's name,
 * with `\r\n` read as `\n` and nothing else normalised. The starter launchers are read from the default branch with
 * the account's sign-in. The answer is advice; the moderation stays the authority, and a problem reading the starter
 * launchers never stops a send.
 */
class LauncherCheck(
    private val environment: RumbleEnvironment,
    private val api: GitHubApi,
) {
    private val repository = "/repos/${RumbleEnvironments.owner}/${environment.botsRepository}"

    /** Never throws: whatever goes wrong is [LauncherState.NotChecked] with a plain reason. */
    fun compare(tokens: GitHubTokens, staged: StagedSubmission): LauncherState =
        try {
            compareOrThrow(tokens, staged)
        } catch (e: RuntimeException) {
            LauncherState.NotChecked("the launchers could not be compared (${e.javaClass.simpleName})")
        }

    private fun compareOrThrow(tokens: GitHubTokens, staged: StagedSubmission): LauncherState {
        if (!LANGUAGE.matches(staged.language)) return LauncherState.NotChecked("the language ${staged.language} has no starter launcher folder")
        // Both of the bot's own files are read first, so nothing is asked of GitHub when one of them cannot be read.
        val own = SUFFIXES.associateWith { suffix ->
            val file = staged.name + suffix
            try {
                Files.readAllBytes(staged.botFolder.resolve(file))
            } catch (e: IOException) {
                return LauncherState.NotChecked("the launcher $file could not be read from the bot's copy")
            }
        }
        val different = mutableListOf<String>()
        for ((suffix, bytes) in own) {
            val starter = when (val read = starter(tokens, staged.language, suffix)) {
                is Read.Text -> read.text
                is Read.Missing -> return LauncherState.NotChecked(read.reason)
            }
            // A launcher that is not text in UTF-8 cannot equal the starter launcher.
            val actual = strictText(bytes)
            if (actual == null || lines(starter.replace(STARTER, staged.name)) != lines(actual)) different += staged.name + suffix
        }
        return if (different.isEmpty()) LauncherState.Same else LauncherState.Differs(different)
    }

    private sealed interface Read {
        class Text(val text: String) : Read
        class Missing(val reason: String) : Read
    }

    private fun starter(tokens: GitHubTokens, language: String, suffix: String): Read {
        val answer = try {
            api.get("$repository/contents/templates/$language/$STARTER/$STARTER$suffix", tokens.accessToken)
        } catch (e: IOException) {
            return Read.Missing("GitHub could not be reached, so the starter launchers were not read")
        } catch (e: RuntimeException) {
            return Read.Missing("GitHub could not be asked for the starter launcher (${e.javaClass.simpleName})")
        }
        return when (answer.status) {
            200 -> Read.Text(decode(answer.body) ?: return Read.Missing("GitHub's answer about the starter launcher could not be understood"))
            404 -> Read.Missing("the Rumble has no starter launcher for the language $language")
            401, 403 -> Read.Missing("GitHub did not give the starter launcher to this sign-in (HTTP ${answer.status})")
            else -> Read.Missing("GitHub gave an unexpected answer (HTTP ${answer.status}) about the starter launcher")
        }
    }

    private fun decode(body: String): String? {
        val content = try {
            ((Json.parseToJsonElement(body) as? JsonObject)?.get("content") as? JsonPrimitive)?.contentOrNull
        } catch (e: IllegalArgumentException) {
            null
        } ?: return null
        val bytes = try {
            Base64.getDecoder().decode(content.filterNot { it.isWhitespace() })
        } catch (e: IllegalArgumentException) {
            return null
        }
        return if (bytes.isEmpty()) null else strictText(bytes)
    }

    private fun strictText(bytes: ByteArray): String? =
        try {
            Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (e: CharacterCodingException) {
            null
        }

    private fun lines(text: String) = text.replace("\r\n", "\n")

    companion object {
        private const val STARTER = "StarterBot"
        private val SUFFIXES = listOf(".sh", ".cmd")
        private val LANGUAGE = Regex("[a-z]+")

        /** What the answer says a difference means. */
        const val DIFFERS_MESSAGE = "A moderator must look at this bot, because its launcher differs from the starter launcher."

        /** What the answer says to do about a difference. */
        const val DIFFERS_FIX = "Use the starter launcher for this language, with the bot's name in place of StarterBot."
    }
}
