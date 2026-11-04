package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.io.IOException

/** Where a bot's pull request stands, in one plain state. */
sealed interface BotProgress {
    /** No moderation status exists yet; for a first-time contributor GitHub holds the checks until a moderator approves the run. */
    data object WaitingForChecks : BotProgress

    /** The checks are running. [note] says so when the status wording was not understood. */
    data class BeingChecked(val note: String? = null) : BotProgress

    /** The bot was sent back; [reason] is the moderation's reason in one short line. Push a fix to the same branch. */
    data class SentBack(val reason: String) : BotProgress

    /** The checks could not decide; a human moderator must look at it. */
    data object WaitingForModerator : BotProgress

    /** Moderation passed; the pull request is waiting to be merged. */
    data object Accepted : BotProgress

    /** The pull request is merged and the catalog does not hold the version yet. */
    data object Merged : BotProgress

    /** The catalog holds this version of the bot: it is in the Rumble. */
    data class InCatalog(val version: String) : BotProgress

    /** The pull request was closed without being merged. */
    data object Closed : BotProgress

    /** GitHub did not accept the sign-in; the person must sign in again. */
    data class SignInNeeded(val message: String) : BotProgress

    /** Something else went wrong; [message] says what in plain words. */
    data class Failed(val message: String) : BotProgress
}

/** GitHub did not accept the sign-in while looking for a pull request. */
class SignInRefused : IOException("GitHub did not accept the sign-in. Sign in again.")

/** What the commands need of reading a bot's progress, so a test can stand in for GitHub. */
interface BotProgressReading {
    /** The newest pull request [account] sent for the bot [name], or null when there is none. */
    fun find(tokens: GitHubTokens, account: String, name: String): SentBot?

    fun read(tokens: GitHubTokens, pullRequestNumber: Int, name: String, version: String, catalog: Catalog?): BotProgress
}

/** A pull request an account sent for a bot, and the version it carries. */
data class SentBot(val pullRequestNumber: Int, val version: String)

/**
 * Reads, and only reads, what became of a bot's pull request in the Rumble's bot repository: the pull request, the
 * status and reviews the moderation workflow wrote for its head, and the catalog. It never comments, approves or merges.
 * The moderation workflow's wording can change; wording it does not know is reported as "being checked" with a note,
 * never guessed.
 */
class SubmissionProgress(
    environment: RumbleEnvironment,
    private val api: GitHubApi,
) : BotProgressReading {
    private val upstream = "/repos/${RumbleEnvironments.owner}/${environment.botsRepository}"

    private class Stop(val signInNeeded: Boolean, message: String) : Exception(message, null, false, false)

    /** [catalog] is the Rumble's current catalog, or null when it could not be read; [name] and [version] are the bot sent. */
    override fun read(tokens: GitHubTokens, pullRequestNumber: Int, name: String, version: String, catalog: Catalog?): BotProgress =
        try {
            progress(tokens.accessToken, pullRequestNumber, name, version, catalog)
        } catch (stop: Stop) {
            if (stop.signInNeeded) BotProgress.SignInNeeded(stop.message.orEmpty()) else BotProgress.Failed(stop.message.orEmpty())
        } catch (e: IOException) {
            BotProgress.Failed("GitHub cannot be reached. Try again when you are online.")
        }

    /**
     * Finds the newest pull request that [account] opened for the bot [name], open or not. The program names its
     * branches `submit/<name>-<version>`, which is how the version is found. Anything not found is null; a problem
     * with GitHub throws [IOException] with a plain message.
     */
    override fun find(tokens: GitHubTokens, account: String, name: String): SentBot? {
        val answer = api.get("$upstream/pulls?state=all&sort=created&direction=desc&per_page=100", tokens.accessToken)
        if (answer.status == 401) throw SignInRefused()
        if (answer.status != 200) throw IOException("GitHub gave an unexpected answer (HTTP ${answer.status}). Try again later.")
        val pulls = (parse(answer.body) as? JsonArray) ?: throw IOException(NOT_UNDERSTOOD)
        val prefix = "submit/$name-"
        for (element in pulls) {
            val pull = element as? JsonObject ?: continue
            val author = (pull["user"] as? JsonObject)?.string("login")
            val branch = (pull["head"] as? JsonObject)?.string("ref") ?: continue
            if (!account.equals(author, ignoreCase = true) || !branch.startsWith(prefix)) continue
            val number = (pull["number"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: continue
            val version = branch.removePrefix(prefix).replace(Regex("-\\d+$"), "")
            if (version.isNotEmpty()) return SentBot(number, version)
        }
        return null
    }

    private fun progress(token: String, number: Int, name: String, version: String, catalog: Catalog?): BotProgress {
        val pull = obj(expect(api.get("$upstream/pulls/$number", token)).body) ?: throw Stop(false, NOT_UNDERSTOOD)
        if (pull.string("state") != "open") {
            val merged = (pull["merged"] as? JsonPrimitive)?.booleanOrNull == true
            if (!merged) return BotProgress.Closed
            val inCatalog = catalog?.activeBots?.firstOrNull { it.name == name && it.version == version }
            return if (inCatalog != null) BotProgress.InCatalog(inCatalog.version) else BotProgress.Merged
        }
        val sha = (pull["head"] as? JsonObject)?.string("sha") ?: throw Stop(false, NOT_UNDERSTOOD)
        val status = moderationStatus(token, sha)
        return when (status?.first) {
            null -> BotProgress.WaitingForChecks
            "success" -> BotProgress.Accepted
            "failure", "error" -> BotProgress.SentBack(reason(token, number, sha) ?: status.second.ifEmpty { "The bot did not pass the checks." })
            "pending" -> when (status.second) {
                HUMAN_REVIEW -> BotProgress.WaitingForModerator
                BASE_CHANGED -> BotProgress.BeingChecked()
                else -> BotProgress.BeingChecked(UNKNOWN_WORDING)
            }
            else -> BotProgress.BeingChecked(UNKNOWN_WORDING)
        }
    }

    /** The moderation status's state and description for [sha], or null when there is none. */
    private fun moderationStatus(token: String, sha: String): Pair<String, String>? {
        val answer = expect(api.get("$upstream/commits/$sha/status", token))
        val statuses = (obj(answer.body)?.get("statuses") as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
        val status = statuses.firstOrNull { it.string("context") == CONTEXT } ?: return null
        return (status.string("state") ?: return null) to status.string("description").orEmpty()
    }

    /** The reason in the latest changes-requested review for [sha], in one short line, if there is one. */
    private fun reason(token: String, number: Int, sha: String): String? {
        val answer = expect(api.get("$upstream/pulls/$number/reviews?per_page=100", token))
        val reviews = (parse(answer.body) as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
        val review = reviews.lastOrNull { it.string("state") == "CHANGES_REQUESTED" && it.string("commit_id") == sha } ?: return null
        val line = review.string("body").orEmpty().removePrefix(REQUESTED).lineSequence().firstOrNull().orEmpty()
            .filter { !it.isISOControl() }.trim().take(MAX_REASON)
        return line.ifEmpty { null }
    }

    private fun expect(answer: ApiResponse): ApiResponse {
        if (answer.status == 200) return answer
        throw when {
            answer.status == 401 -> Stop(true, "GitHub did not accept the sign-in. Sign in again.")
            answer.status == 404 -> Stop(false, "GitHub has no such pull request in the Rumble's bot repository.")
            answer.status == 429 || (answer.status == 403 && answer.body.contains("rate limit", ignoreCase = true)) ->
                Stop(false, "GitHub is limiting requests. Try again later.")
            else -> Stop(false, "GitHub gave an unexpected answer (HTTP ${answer.status}). Try again later.")
        }
    }

    private fun parse(text: String): JsonElement? = try {
        Json.parseToJsonElement(text)
    } catch (e: Exception) {
        null
    }

    private fun obj(text: String): JsonObject? = parse(text) as? JsonObject

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    companion object {
        /** The commit status context the moderation workflow reports under. */
        const val CONTEXT = "bot-moderation"
        private const val HUMAN_REVIEW = "Human moderator review required"
        private const val BASE_CHANGED = "Base changed during moderation"
        private const val REQUESTED = "The bot cannot be accepted:"
        private const val MAX_REASON = 300
        private const val NOT_UNDERSTOOD = "GitHub's answer about the pull request could not be understood."
        private const val UNKNOWN_WORDING = "The moderation status was not understood, so this is a best reading."
    }
}
