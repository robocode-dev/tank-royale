package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64

/** What the Rumble knows about a registered account on this computer. None of it is secret. */
class RegistrationRecord(
    val account: String,
    val clientId: String,
    val environment: String,
    val savedAt: Instant,
)

/** Where a registration is remembered, so later runs do not need the network. */
interface RegistrationMemory {
    /** The registration of [account], or null when none is remembered. */
    fun load(account: String): RegistrationRecord?

    /** Remembers [record]. Returns false when it could not be; the registration itself is not lost. */
    fun save(record: RegistrationRecord): Boolean
}

/** How registering an account with the Rumble ended. */
sealed interface RegistrationResult {
    /**
     * The account has [clientId]. [alreadyRegistered] is false when this call registered it. [saved] is false when
     * it could not be remembered on this computer; the registration itself is not lost.
     */
    data class Registered(val account: String, val clientId: String, val alreadyRegistered: Boolean, val saved: Boolean = true) : RegistrationResult

    /**
     * The Rumble refused the registration; [reason] is the workflow's reason in one short line. [earlier] is true when
     * the refusal comes from an earlier issue and no new one was opened. [account] is the account that was refused.
     */
    data class Refused(val reason: String, val earlier: Boolean = false, val account: String? = null) : RegistrationResult

    /** The registration was not finished in time. A later try continues; [issueUrl] is the registration issue. */
    data class NotFinished(val issueUrl: String?) : RegistrationResult

    /** GitHub did not accept the sign-in; the person must sign in again. */
    data class SignInNeeded(val message: String) : RegistrationResult

    /** Something else went wrong; [message] says what in plain words. */
    data class Failed(val message: String) : RegistrationResult
}

/**
 * Registers the signed-in account as a Rumble participant, as the outside-account experiment proved: the account
 * opens a registration issue in the environment's result repository and a trusted workflow there registers the
 * account that opened it. The registration file in that repository is the proof of success; the workflow's reply is
 * read only to learn why a registration was refused.
 */
class Registration(
    private val environment: RumbleEnvironment,
    private val api: GitHubApi,
    private val store: RegistrationMemory,
    private val clock: Clock = Clock.systemUTC(),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
    private val pollSeconds: Int = 5,
    private val maxWaitSeconds: Int = 120,
) {
    private val repository = "/repos/${RumbleEnvironments.owner}/${environment.dataRepository}"

    private class Stop(val result: RegistrationResult) : Exception(null, null, false, false)

    /**
     * Registers the account of [tokens]. The workflow closes every registration issue, refused or not, so a refusal on
     * the account's newest closed issue is answered without opening another. [retry] opens one new issue anyway, for a
     * person who has fixed the reason.
     */
    fun register(tokens: GitHubTokens, retry: Boolean = false): RegistrationResult =
        try {
            val token = tokens.accessToken
            val account = whoAmI(token)

            store.load(account)?.let { return RegistrationResult.Registered(it.account, it.clientId, alreadyRegistered = true) }
            readRegistrationFile(account, token)?.let { return known(account, it, alreadyRegistered = true) }

            val open = openRegistrationIssue(account, token)
            if (open == null && !retry) {
                earlierRefusal(account, token)?.let { return RegistrationResult.Refused(it, earlier = true, account = account) }
            }
            waitForRegistration(account, token, open ?: createRegistrationIssue(account, token))
        } catch (stop: Stop) {
            stop.result
        } catch (e: IOException) {
            RegistrationResult.Failed("GitHub cannot be reached. Try again when you are online.")
        }

    private class Issue(val number: Int, val url: String?)

    private fun whoAmI(token: String): String {
        val answer = api.get("/user", token)
        refuseBadToken(answer)
        if (answer.status != 200) throw Stop(RegistrationResult.Failed(unexpected(answer)))
        return GitHubAccount.nameIn(answer) ?: throw Stop(RegistrationResult.Failed("GitHub did not say which account is signed in."))
    }

    /** The client ID in the account's registration file, or null if there is none yet. */
    internal fun readRegistrationFile(account: String, token: String?): String? {
        val answer = api.get("$repository/contents/clients/$account.json", token)
        if (answer.status == 404) return null
        refuseBadToken(answer)
        if (answer.status != 200) throw Stop(RegistrationResult.Failed(unexpected(answer)))
        val encoded = obj(answer.body)?.string("content")
        val text = try {
            encoded?.let { String(Base64.getMimeDecoder().decode(it), Charsets.UTF_8) }
        } catch (e: IllegalArgumentException) {
            null
        }
        val file = text?.let { obj(it) }
        val clientId = ((file?.get("clientIds") as? JsonArray)?.firstOrNull() as? JsonPrimitive)?.contentOrNull
        if (file == null || !account.equals(file.string("account"), ignoreCase = true) || clientId == null || !CLIENT_ID.matches(clientId)) {
            throw Stop(RegistrationResult.Failed("The registration file in the Rumble result repository is not valid."))
        }
        return clientId
    }

    private fun known(account: String, clientId: String, alreadyRegistered: Boolean): RegistrationResult {
        val saved = store.save(RegistrationRecord(account, clientId, environment.name, clock.instant()))
        return RegistrationResult.Registered(account, clientId, alreadyRegistered, saved)
    }

    /** An earlier registration issue from this account that the workflow has not answered yet, if there is one. */
    private fun openRegistrationIssue(account: String, token: String): Issue? {
        val answer = api.get("$repository/issues?creator=$account&state=open&per_page=30", token)
        refuseBadToken(answer)
        if (answer.status != 200) throw Stop(RegistrationResult.Failed(unexpected(answer)))
        val issues = (parse(answer.body) as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
        val earlier = issues.firstOrNull { it.string("title").orEmpty().startsWith(TITLE_PREFIX) && it["pull_request"] == null }
        return earlier?.let { issue(it) }
    }

    /**
     * The refusal on the account's newest closed registration issue, if that is how it ended. The newest issue decides;
     * only the newest hundred of the account's closed issues are read, and missing an older one costs one new issue.
     */
    private fun earlierRefusal(account: String, token: String): String? {
        val answer = api.get("$repository/issues?creator=$account&state=closed&sort=created&direction=desc&per_page=100", token)
        refuseBadToken(answer)
        if (answer.status != 200) throw Stop(RegistrationResult.Failed(unexpected(answer)))
        val issues = arrayIn(answer).mapNotNull { it as? JsonObject }
        val newest = issues.firstOrNull { it.string("title").orEmpty().startsWith(TITLE_PREFIX) && it["pull_request"] == null }
        return newest?.let { issue(it) }?.let { found ->
            // Not knowing how it ended must not open a new public issue, as it would if this counted as no refusal.
            val comments = commentsOf(found.number, token)
            refuseBadToken(comments)
            if (comments.status != 200) throw Stop(RegistrationResult.Failed(unexpected(comments)))
            arrayIn(comments)
            refusalIn(comments)
        }
    }

    /** The list in [answer]. An answer that is no list says nothing about how an issue ended, so it stops the run. */
    private fun arrayIn(answer: ApiResponse): JsonArray =
        parse(answer.body) as? JsonArray ?: throw Stop(RegistrationResult.Failed(unexpected(answer)))

    private fun createRegistrationIssue(account: String, token: String): Issue {
        val body = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(
                mapOf(
                    "title" to JsonPrimitive("$TITLE_PREFIX $account"),
                    "body" to JsonPrimitive("Please register my GitHub account as a Rumble participant."),
                )
            ),
        )
        val answer = api.postJson("$repository/issues", token, body)
        refuseBadToken(answer)
        if (answer.status != 201) {
            throw Stop(
                RegistrationResult.Failed(
                    "The Rumble sign-in App could not open a registration issue in ${RumbleEnvironments.owner}/${environment.dataRepository}. " +
                        "Check that the App is installed there."
                )
            )
        }
        return issue(obj(answer.body)) ?: throw Stop(RegistrationResult.Failed(unexpected(answer)))
    }

    private fun waitForRegistration(account: String, token: String, issue: Issue): RegistrationResult {
        val deadline = clock.instant().plusSeconds(maxWaitSeconds.toLong())
        while (true) {
            readRegistrationFile(account, token)?.let { return known(account, it, alreadyRegistered = false) }
            refusalReason(issue.number, token)?.let { return RegistrationResult.Refused(it, account = account) }
            if (!clock.instant().isBefore(deadline)) return RegistrationResult.NotFinished(issue.url)
            sleep(Duration.ofSeconds(pollSeconds.toLong()).toMillis())
        }
    }

    /**
     * The reason in the workflow's own comment. The issues are public, so anybody can write a comment that starts with
     * the same words; only the account the workflow comments under counts, and nobody else can take that name.
     */
    private fun refusalReason(number: Int, token: String): String? {
        val answer = commentsOf(number, token)
        return if (answer.status != 200) null else refusalIn(answer)
    }

    private fun commentsOf(number: Int, token: String): ApiResponse = api.get("$repository/issues/$number/comments?per_page=100", token)

    private fun refusalIn(answer: ApiResponse): String? {
        val comments = (parse(answer.body) as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
            .filter { (it["user"] as? JsonObject)?.string("login") == WORKFLOW_ACCOUNT }
            .mapNotNull { it.string("body") }
        val refusal = comments.firstOrNull { it.trimStart().startsWith(REFUSED) } ?: return null
        val reason = refusal.trimStart().removePrefix(REFUSED).lineSequence().firstOrNull().orEmpty()
            .filter { !it.isISOControl() }.trim().take(MAX_REASON)
        return reason.ifEmpty { "The Rumble refused the registration." }
    }

    private fun refuseBadToken(answer: ApiResponse) {
        if (answer.status == 401) throw Stop(RegistrationResult.SignInNeeded("GitHub did not accept the sign-in. Sign in again."))
    }

    private fun unexpected(answer: ApiResponse): String =
        if (answer.status == 403 || answer.status == 429) "GitHub is limiting requests. Try again later."
        else "GitHub gave an unexpected answer (HTTP ${answer.status}). Try again later."

    private fun issue(o: JsonObject?): Issue? {
        val number = (o?.get("number") as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: return null
        return Issue(number, o.string("html_url"))
    }

    private fun parse(text: String): JsonElement? = try {
        Json.parseToJsonElement(text)
    } catch (e: Exception) {
        null
    }

    private fun obj(text: String): JsonObject? = parse(text) as? JsonObject

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    companion object {
        const val TITLE_PREFIX = "[register]"
        private const val REFUSED = "Registration refused:"
        private const val WORKFLOW_ACCOUNT = "github-actions[bot]"
        private const val MAX_REASON = 200
        private val CLIENT_ID = Regex("[A-Za-z0-9._-]{1,80}")
    }
}
