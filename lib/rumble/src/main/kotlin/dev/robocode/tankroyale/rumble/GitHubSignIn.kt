package dev.robocode.tankroyale.rumble

import java.io.IOException
import java.time.Clock
import java.time.Duration

/** Raised when an environment has no sign-in App, before GitHub is contacted. */
class SignInUnavailable(message: String) : Exception(message)

/** Raised when GitHub cannot give a sign-in code. The message says what to do in plain words. */
class SignInFailed(message: String) : Exception(message)

/** One answer when checking whether the person has approved a sign-in code. */
sealed interface SignInStep {
    /** Not approved yet; check again after the interval. */
    data object Waiting : SignInStep

    /** GitHub asks for a slower pace; use [intervalSeconds] from now on. */
    data class SlowDown(val intervalSeconds: Int) : SignInStep

    data class SignedIn(val tokens: GitHubTokens) : SignInStep

    /** The code ran out of time; start again. */
    data object Expired : SignInStep

    /** The person refused on GitHub's page. */
    data object Denied : SignInStep

    /** The caller stopped waiting. */
    data object Cancelled : SignInStep

    /** [codeStillUsable] is true when the code itself was not the problem, so checking again can still work. */
    data class Failed(val message: String, val codeStillUsable: Boolean = false) : SignInStep
}

/** The result of renewing a sign-in. */
sealed interface RefreshResult {
    data class Renewed(val tokens: GitHubTokens) : RefreshResult

    /** GitHub will not renew it (access revoked or the refresh token expired); the person must sign in again. */
    data class Refused(val message: String) : RefreshResult

    /** The renewal could not be tried or answered; trying again later may work. */
    data class Failed(val message: String) : RefreshResult
}

/**
 * Signs a person in to GitHub with a short code and renews the sign-in, as the outside-account experiment proved.
 * It needs only the sign-in App's public identifier, never a client secret. The tokens stay in memory.
 */
class GitHubSignIn(
    private val environment: RumbleEnvironment,
    private val transport: GitHubAuthTransport,
    private val clock: Clock = Clock.systemUTC(),
) {

    /** Asks GitHub for a sign-in code to show the person. */
    fun start(): DeviceCode {
        val clientId = environment.signInClientId
            ?: throw SignInUnavailable("Signing in is not available for the ${environment.displayName} Rumble yet.")
        val answer = try {
            transport.postForm(DEVICE_CODE_URL, mapOf("client_id" to clientId))
        } catch (e: IOException) {
            throw SignInFailed(OFFLINE)
        }
        val fields = answer.fields
        val deviceCode = fields["device_code"]
        val userCode = fields["user_code"]
        val uri = fields["verification_uri"]
        val expiresIn = fields["expires_in"]?.toLongOrNull()
        val interval = fields["interval"]?.toIntOrNull()
        if (answer.status != 200 || deviceCode == null || userCode == null || uri == null || expiresIn == null || interval == null) {
            throw SignInFailed(describeProblem(answer))
        }
        return DeviceCode(userCode, uri, clock.instant().plusSeconds(expiresIn), interval, deviceCode)
    }

    /** Checks once whether the person has approved [code]. [intervalSeconds] is the pace in use now. */
    fun check(code: DeviceCode, intervalSeconds: Int = code.intervalSeconds): SignInStep {
        val clientId = environment.signInClientId
            ?: return SignInStep.Failed("Signing in is not available for the ${environment.displayName} Rumble yet.")
        val answer = try {
            transport.postForm(
                TOKEN_URL,
                mapOf("client_id" to clientId, "device_code" to code.deviceCode, "grant_type" to DEVICE_GRANT),
            )
        } catch (e: IOException) {
            return SignInStep.Failed(OFFLINE, codeStillUsable = true)
        }
        tokensOf(answer)?.let { return SignInStep.SignedIn(it) }
        return when (errorOf(answer)) {
            "authorization_pending" -> SignInStep.Waiting
            "slow_down" -> SignInStep.SlowDown(answer.fields["interval"]?.toIntOrNull() ?: (intervalSeconds + SLOW_DOWN_SECONDS))
            "expired_token" -> SignInStep.Expired
            "access_denied" -> SignInStep.Denied
            "device_flow_disabled" -> SignInStep.Failed("Signing in with a code is switched off for this GitHub App.")
            else -> SignInStep.Failed(describeProblem(answer), codeStillUsable = errorOf(answer) == null)
        }
    }

    /**
     * Waits for the person to approve [code], checking no more often than GitHub allows. [sleep] waits the given
     * milliseconds and [cancelled] says whether the caller has stopped waiting. Returns the final step: signed in,
     * expired, denied, failed or cancelled.
     */
    fun awaitSignIn(code: DeviceCode, sleep: (Long) -> Unit, cancelled: () -> Boolean): SignInStep {
        var interval = code.intervalSeconds
        while (true) {
            if (cancelled()) return SignInStep.Cancelled
            if (!clock.instant().isBefore(code.expiresAt)) return SignInStep.Expired
            sleep(Duration.ofSeconds(interval.toLong()).toMillis())
            if (cancelled()) return SignInStep.Cancelled
            if (!clock.instant().isBefore(code.expiresAt)) return SignInStep.Expired
            when (val step = check(code, interval)) {
                SignInStep.Waiting -> Unit
                is SignInStep.SlowDown -> interval = step.intervalSeconds
                else -> return step
            }
        }
    }

    /** Renews [tokens] with their refresh token. No client secret is sent. */
    fun refresh(tokens: GitHubTokens): RefreshResult {
        val clientId = environment.signInClientId
            ?: return RefreshResult.Failed("Signing in is not available for the ${environment.displayName} Rumble yet.")
        val refreshToken = tokens.refreshToken
            ?: return RefreshResult.Refused("This sign-in cannot be renewed. Sign in again.")
        val answer = try {
            transport.postForm(
                TOKEN_URL,
                mapOf("client_id" to clientId, "grant_type" to "refresh_token", "refresh_token" to refreshToken),
            )
        } catch (e: IOException) {
            return RefreshResult.Failed(OFFLINE)
        }
        tokensOf(answer)?.let { return RefreshResult.Renewed(it) }
        return when (errorOf(answer)) {
            "bad_refresh_token" -> RefreshResult.Refused("GitHub access was revoked or has expired. Sign in again.")
            else -> RefreshResult.Failed(describeProblem(answer))
        }
    }

    private fun tokensOf(answer: AuthResponse): GitHubTokens? {
        val access = answer.fields["access_token"] ?: return null
        val now = clock.instant()
        return GitHubTokens(
            accessToken = access,
            refreshToken = answer.fields["refresh_token"],
            expiresAt = answer.fields["expires_in"]?.toLongOrNull()?.let { now.plusSeconds(it) },
            refreshTokenExpiresAt = answer.fields["refresh_token_expires_in"]?.toLongOrNull()?.let { now.plusSeconds(it) },
        )
    }

    private fun errorOf(answer: AuthResponse): String? = answer.fields["error"]?.takeIf { SAFE_ERROR.matches(it) }

    /** A plain-words description that never repeats what GitHub sent, so no token can leak through it. */
    private fun describeProblem(answer: AuthResponse): String {
        val code = errorOf(answer)
        return if (code != null) "GitHub did not accept the sign-in ($code)."
        else "GitHub gave an unexpected answer (HTTP ${answer.status}). Try again later."
    }

    companion object {
        const val DEVICE_CODE_URL = "https://github.com/login/device/code"
        const val TOKEN_URL = "https://github.com/login/oauth/access_token"
        private const val DEVICE_GRANT = "urn:ietf:params:oauth:grant-type:device_code"
        private const val SLOW_DOWN_SECONDS = 5
        private const val OFFLINE = "GitHub cannot be reached. Try again when you are online."
        private val SAFE_ERROR = Regex("[a-z_]{1,40}")
    }
}
