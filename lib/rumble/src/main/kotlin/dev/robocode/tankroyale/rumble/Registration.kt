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
import java.util.Base64

/** How registering an account with the Rumble ended. */
sealed interface RegistrationResult {
    /** The account has [clientId]. [alreadyRegistered] is false when this call registered it. */
    data class Registered(val account: String, val clientId: String, val alreadyRegistered: Boolean) : RegistrationResult

    /** The Rumble refused the registration; [reason] is the workflow's reason in one short line. */
    data class Refused(val reason: String) : RegistrationResult

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
    private val store: RegistrationStore,
    private val clock: Clock = Clock.systemUTC(),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
    private val pollSeconds: Int = 5,
    private val maxWaitSeconds: Int = 120,
) {
    private val repository = "/repos/${RumbleEnvironments.owner}/${environment.dataRepository}"

    private class Stop(val result: RegistrationResult) : Exception(null, null, false, false)

    fun register(tokens: GitHubTokens): RegistrationResult =
        try {
            val token = tokens.accessToken
            val account = whoAmI(token)

            store.load(account)?.let { return RegistrationResult.Registered(it.account, it.clientId, alreadyRegistered = true) }
            readRegistrationFile(account, token)?.let { return known(account, it, alreadyRegistered = true) }

            val issue = openRegistrationIssue(account, token) ?: createRegistrationIssue(account, token)
            waitForRegistration(account, token, issue)
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
        val login = obj(answer.body)?.string("login")
        if (login == null || !ACCOUNT.matches(login)) throw Stop(RegistrationResult.Failed("GitHub did not say which account is signed in."))
        return login
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
        store.save(RegistrationRecord(account, clientId, environment.name, clock.instant()))
        return RegistrationResult.Registered(account, clientId, alreadyRegistered)
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
            refusalReason(issue.number, token)?.let { return RegistrationResult.Refused(it) }
            if (!clock.instant().isBefore(deadline)) return RegistrationResult.NotFinished(issue.url)
            sleep(Duration.ofSeconds(pollSeconds.toLong()).toMillis())
        }
    }

    private fun refusalReason(number: Int, token: String): String? {
        val answer = api.get("$repository/issues/$number/comments?per_page=30", token)
        if (answer.status != 200) return null
        val comments = (parse(answer.body) as? JsonArray).orEmpty().mapNotNull { (it as? JsonObject)?.string("body") }
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
        private const val MAX_REASON = 200
        private val ACCOUNT = Regex("[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})")
        private val CLIENT_ID = Regex("[A-Za-z0-9._-]{1,80}")
    }
}
