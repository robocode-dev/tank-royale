package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant

/** How asking for a sign-in code ended. */
sealed interface SignInStart {
    /** The person should enter [code]. [reused] is true when it is the code already waiting. */
    data class Started(val code: WaitingSignIn, val reused: Boolean) : SignInStart

    /** A saved sign-in was renewed, so it still works. [account] is null when GitHub would not say which it is. */
    data class AlreadySignedIn(val account: String?) : SignInStart

    /** This environment has no sign-in App. */
    data class Unavailable(val message: String) : SignInStart

    /** This computer cannot keep a sign-in private, so no code was asked for. */
    data class NotPrivate(val message: String) : SignInStart

    data class Failed(val message: String) : SignInStart
}

/** How checking for the person's approval ended. */
sealed interface SignInFinish {
    /** Signed in and saved. [account] is null when GitHub would not say which it is; the next login or register fills it in. */
    data class SignedIn(val account: String?) : SignInFinish

    /** Not approved yet; check again after [retryAfterSeconds]. */
    data class Waiting(val code: WaitingSignIn, val retryAfterSeconds: Long) : SignInFinish

    /** GitHub approved, but the sign-in could not be kept. The code is used up, so the person must start again. */
    data class NotSaved(val reason: String) : SignInFinish

    data object NoWaitingCode : SignInFinish
    data object CodeExpired : SignInFinish
    data object CodeDenied : SignInFinish
    data class Unavailable(val message: String) : SignInFinish

    /** The check failed. [codeStillWaiting] says whether checking again can still work, after [retryAfterSeconds]. */
    data class Failed(val message: String, val codeStillWaiting: Boolean, val retryAfterSeconds: Long = 0) : SignInFinish
}

/** What signing out did. [removed] is false when what is saved could not be removed. */
data class SignOutResult(val removedSomething: Boolean, val removed: Boolean)

/**
 * The sign-in as a series of separate runs of a program: asking for a code, checking for the person's approval and
 * signing out. Between runs the code waits, protected, in the same file as the saved sign-in.
 *
 * The lock on the saved sign-in is held for one check at a time and the waiting code is read again every round, so a
 * wait blocks nobody for longer than one request to GitHub, and a run that is waiting cannot bring a sign-in back after
 * the person has signed out. Whatever is learned outside the lock, like an account name, is saved for the sign-in it
 * was learned for, by its identity, so it can never land on a later one.
 */
class SignInFlow(
    private val environment: RumbleEnvironment,
    private val signIn: GitHubSignIn,
    private val store: SignInStore,
    private val api: GitHubApi,
    private val clock: Clock = Clock.systemUTC(),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
) {
    private val noApp get() = environment.signInNotAvailable()

    /** Renews the saved sign-in. */
    fun renewSaved(): SavedRenewal = SavedSignIn(store, signIn).renew()

    /** Asks for a code unless the person is already signed in or a code is already waiting. */
    fun start(): SignInStart {
        if (environment.signInClientId == null) return SignInStart.Unavailable(noApp)

        if (store.load() is LoadResult.Found) {
            when (val renewal = renewSaved()) {
                is SavedRenewal.Renewed -> if (renewal.kept) return SignInStart.AlreadySignedIn(identify(renewal) ?: renewal.account)
                is SavedRenewal.Failed -> return SignInStart.Failed(renewal.message)
                is SavedRenewal.SignInNeeded -> Unit // GitHub refused it and it is gone, with all that belonged to it.
            }
        }
        if (!store.canKeepPrivate()) {
            return SignInStart.NotPrivate("This computer cannot keep a saved sign-in private to you, so signing in was not started.")
        }
        return store.locked { startLocked() }
    }

    private fun startLocked(): SignInStart {
        val now = clock.instant()
        store.loadWaiting()?.let { waiting ->
            // A code about to run out is no use to a person who still has to open the page.
            if (now.plus(MIN_TIME_TO_TYPE).isBefore(waiting.expiresAt)) return SignInStart.Started(waiting, reused = true)
            store.forgetWaiting()
        }
        val code = try {
            signIn.start()
        } catch (e: SignInFailed) {
            return SignInStart.Failed(e.message.orEmpty())
        }
        val waiting = WaitingSignIn(
            code.userCode, code.verificationUri, code.expiresAt, code.intervalSeconds,
            now.plusSeconds(code.intervalSeconds.toLong()), code.deviceCode,
        )
        return when (val saved = store.saveWaiting(waiting)) {
            SaveResult.Saved -> SignInStart.Started(waiting, reused = false)
            // The folder passed the check for privacy just before, so this is an ordinary failure, such as a full disk.
            is SaveResult.NotSaved -> SignInStart.Failed(saved.reason)
        }
    }

    /**
     * Checks for approval, never faster than GitHub allows, and for no longer than [waitSeconds]; 0 means one check at
     * most. A check that GitHub's pace does not allow yet is answered without contacting GitHub.
     */
    fun finish(waitSeconds: Long = 0): SignInFinish {
        if (environment.signInClientId == null) return SignInFinish.Unavailable(noApp)
        val deadline = clock.instant().plusSeconds(waitSeconds)
        while (true) {
            // Nothing to check needs no lock, and taking one would leave a lock file where nothing was saved.
            if (!store.hasSavedFiles()) return SignInFinish.NoWaitingCode
            var approved: Pair<GitHubTokens, String>? = null
            val round = store.locked { checkLocked { tokens, signInId -> approved = tokens to signInId } }
            approved?.let { (tokens, signInId) -> return SignInFinish.SignedIn(identify(tokens, signInId)) }
            val next = when {
                round is SignInFinish.Waiting -> round.code.nextCheckAt
                // A short outage is no reason to stop while the person may still be typing the code.
                round is SignInFinish.Failed && round.codeStillWaiting -> clock.instant().plusSeconds(round.retryAfterSeconds)
                else -> return round
            }
            if (next.isAfter(deadline)) return round
            val pause = Duration.between(clock.instant(), next).toMillis()
            if (pause > 0) sleep(pause)
        }
    }

    /** One round under the lock. [onSaved] receives a new sign-in once it is saved; its name is found outside the lock. */
    private fun checkLocked(onSaved: (GitHubTokens, String) -> Unit): SignInFinish {
        val now = clock.instant()
        val waiting = store.loadWaiting()
            ?: return (store.load() as? LoadResult.Found)?.let { SignInFinish.SignedIn(it.account) } ?: SignInFinish.NoWaitingCode
        if (!now.isBefore(waiting.expiresAt)) {
            store.forgetWaiting()
            return SignInFinish.CodeExpired
        }
        if (waiting.nextCheckAt.isAfter(now)) return waitingFor(waiting, now)

        val deviceCode = DeviceCode(waiting.userCode, waiting.verificationUri, waiting.expiresAt, waiting.intervalSeconds, waiting.deviceCode)
        return when (val step = signIn.check(deviceCode, waiting.intervalSeconds)) {
            SignInStep.Waiting -> again(waiting, waiting.intervalSeconds, now)
            is SignInStep.SlowDown -> again(waiting, step.intervalSeconds, now)
            is SignInStep.SignedIn -> approved(step.tokens, onSaved)
            SignInStep.Expired -> {
                store.forgetWaiting()
                SignInFinish.CodeExpired
            }
            SignInStep.Denied -> {
                store.forgetWaiting()
                SignInFinish.CodeDenied
            }
            SignInStep.Cancelled -> SignInFinish.Failed("The check was stopped.", true, waiting.intervalSeconds.toLong())
            is SignInStep.Failed ->
                if (step.codeStillUsable) {
                    // A longer pause after a failure, kept between runs so repeated runs do not press a struggling
                    // GitHub. Only this pause is longer: the pace returns to normal when GitHub answers again.
                    val pause = maxOf(waiting.intervalSeconds, minOf(waiting.intervalSeconds * 2, MAX_RETRY_SECONDS))
                    store.saveWaiting(waiting.paced(waiting.intervalSeconds, now.plusSeconds(pause.toLong())))
                    SignInFinish.Failed(step.message, true, pause.toLong())
                } else {
                    store.forgetWaiting()
                    SignInFinish.Failed(step.message, false)
                }
        }
    }

    private fun again(waiting: WaitingSignIn, interval: Int, now: Instant): SignInFinish {
        val next = waiting.paced(interval, now.plusSeconds(interval.toLong()))
        // Not saving the pace only means GitHub may ask for a slower one again; the answer stays true.
        store.saveWaiting(next)
        return waitingFor(next, now)
    }

    private fun waitingFor(waiting: WaitingSignIn, now: Instant): SignInFinish {
        val millis = Duration.between(now, waiting.nextCheckAt).toMillis().coerceAtLeast(0)
        return SignInFinish.Waiting(waiting, (millis + 999) / 1000)
    }

    private fun approved(tokens: GitHubTokens, onSaved: (GitHubTokens, String) -> Unit): SignInFinish {
        val refresh = tokens.refreshToken
        val saved = if (refresh == null) SaveResult.NotSaved("GitHub gave no refresh token, so this sign-in cannot be saved.")
        else store.beginSignIn(refresh, tokens.refreshTokenExpiresAt)
        if (saved is SaveResult.NotSaved) {
            store.forgetWaiting() // It is used up, so checking it again could never work.
            return SignInFinish.NotSaved(saved.reason)
        }
        onSaved(tokens, (store.load() as LoadResult.Found).signInId)
        return SignInFinish.SignedIn(null)
    }

    /** Finds the account of the sign-in that [renewal] renewed and saves its name. Returns null when GitHub cannot say. */
    fun identify(renewal: SavedRenewal.Renewed): String? = identify(renewal.tokens, renewal.signInId)

    private fun identify(tokens: GitHubTokens, signInId: String): String? {
        val name = try {
            GitHubAccount.nameIn(api.get("/user", tokens.accessToken))
        } catch (e: IOException) {
            null
        } ?: return null
        store.saveAccount(signInId, name)
        return name
    }

    /** Where a registration is remembered for the sign-in [renewal] renewed. */
    fun registrationMemory(renewal: SavedRenewal.Renewed): RegistrationMemory = store.registrationFor(renewal.signInId)

    /** True when the sign-in [renewal] renewed is still the saved one. */
    fun stillSignedIn(renewal: SavedRenewal.Renewed): Boolean = (store.load() as? LoadResult.Found)?.signInId == renewal.signInId

    /** Removes what is saved for this environment, and creates nothing when nothing is saved. */
    fun signOut(): SignOutResult {
        if (!store.hasSavedFiles()) return SignOutResult(removedSomething = false, removed = true)
        return store.locked { SignOutResult(removedSomething = true, removed = store.forget()) }
    }

    private companion object {
        const val MAX_RETRY_SECONDS = 60
        val MIN_TIME_TO_TYPE: Duration = Duration.ofMinutes(1)
    }
}

/** What to say when [this] environment has no sign-in App. */
fun RumbleEnvironment.signInNotAvailable(): String = "Signing in is not available for the $displayName Rumble yet."

/** Reads the account name out of GitHub's answer to `/user`. */
internal object GitHubAccount {
    private val NAME = Regex("[A-Za-z0-9](?:[A-Za-z0-9-]{0,38})")

    /** The account name in [answer], or null when the answer does not hold a valid one. */
    fun nameIn(answer: ApiResponse): String? {
        if (answer.status != 200) return null
        val body = try {
            Json.parseToJsonElement(answer.body) as? JsonObject
        } catch (e: IllegalArgumentException) {
            null
        }
        return (body?.get("login") as? JsonPrimitive)?.contentOrNull?.takeIf { NAME.matches(it) }
    }
}
