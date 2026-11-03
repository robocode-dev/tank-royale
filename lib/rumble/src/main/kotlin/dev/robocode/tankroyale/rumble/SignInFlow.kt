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

    /** The check failed. [codeStillWaiting] says whether checking again can still work. */
    data class Failed(val message: String, val codeStillWaiting: Boolean) : SignInFinish
}

/** What signing out did. [remaining] names what could not be removed. */
data class SignOutResult(val removedSomething: Boolean, val remaining: List<String>)

/**
 * The sign-in as a series of separate runs of a program: asking for a code, checking for the person's approval and
 * signing out. Between runs the code waits, protected, beside the saved sign-in.
 *
 * The lock on the saved sign-in is held for one check at a time and the waiting code is read again every round, so a
 * wait blocks nobody for longer than one request to GitHub, and a run that is waiting cannot bring a sign-in back after
 * the person has signed out. The lookup of an account name happens outside the lock, and its result is saved only if
 * the sign-in is still there.
 */
class SignInFlow(
    private val environment: RumbleEnvironment,
    private val signIn: GitHubSignIn,
    private val store: SignInStore,
    private val account: SavedAccountStore,
    private val registration: RegistrationStore,
    private val api: GitHubApi,
    private val clock: Clock = Clock.systemUTC(),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
) {
    private val noApp get() = "Signing in is not available for the ${environment.displayName} Rumble yet."

    /** Asks for a code unless the person is already signed in or a code is already waiting. */
    fun start(): SignInStart {
        if (environment.signInClientId == null) return SignInStart.Unavailable(noApp)

        if (store.load() is LoadResult.Found) {
            when (val renewal = SavedSignIn(store, signIn).renew()) {
                is SavedRenewal.Renewed -> if (renewal.kept) return SignInStart.AlreadySignedIn(identify(renewal.tokens) ?: account.load())
                is SavedRenewal.Failed -> return SignInStart.Failed(renewal.message)
                is SavedRenewal.SignInNeeded -> account.forget() // GitHub refused it and it is gone; a new code follows.
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
            if (now.isBefore(waiting.expiresAt)) return SignInStart.Started(waiting, reused = true)
            store.forgetWaiting()
        }
        val code = try {
            signIn.start()
        } catch (e: SignInUnavailable) {
            return SignInStart.Unavailable(noApp)
        } catch (e: SignInFailed) {
            return SignInStart.Failed(e.message.orEmpty())
        }
        val waiting = WaitingSignIn(
            code.userCode, code.verificationUri, code.expiresAt, code.intervalSeconds,
            now.plusSeconds(code.intervalSeconds.toLong()), code.deviceCode,
        )
        return when (val saved = store.saveWaiting(waiting)) {
            SaveResult.Saved -> SignInStart.Started(waiting, reused = false)
            is SaveResult.NotSaved -> SignInStart.NotPrivate(saved.reason)
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
            var approvedTokens: GitHubTokens? = null
            val round = store.locked { checkLocked { approvedTokens = it } }
            approvedTokens?.let { return SignInFinish.SignedIn(identifyNewSignIn(it)) }
            if (round !is SignInFinish.Waiting) return round
            val next = round.code.nextCheckAt
            if (next.isAfter(deadline)) return round
            val pause = Duration.between(clock.instant(), next).toMillis()
            if (pause > 0) sleep(pause)
        }
    }

    /** One round under the lock. [onSaved] receives a new sign-in once it is saved; its name is found outside the lock. */
    private fun checkLocked(onSaved: (GitHubTokens) -> Unit): SignInFinish {
        val now = clock.instant()
        val waiting = store.loadWaiting()
            ?: return if (store.load() is LoadResult.Found) SignInFinish.SignedIn(account.load()) else SignInFinish.NoWaitingCode
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
            SignInStep.Cancelled -> SignInFinish.Failed("The check was stopped.", codeStillWaiting = true)
            is SignInStep.Failed -> {
                if (!step.codeStillUsable) store.forgetWaiting()
                SignInFinish.Failed(step.message, step.codeStillUsable)
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

    private fun approved(tokens: GitHubTokens, onSaved: (GitHubTokens) -> Unit): SignInFinish {
        store.forgetWaiting()
        val refresh = tokens.refreshToken
            ?: return SignInFinish.NotSaved("GitHub gave no refresh token, so this sign-in cannot be saved.")
        return when (val saved = store.save(refresh, tokens.refreshTokenExpiresAt)) {
            is SaveResult.NotSaved -> SignInFinish.NotSaved(saved.reason)
            SaveResult.Saved -> {
                // This may be another account, so nothing saved for the earlier one may be taken for this one.
                account.forget()
                onSaved(tokens)
                SignInFinish.SignedIn(null)
            }
        }
    }

    private fun identifyNewSignIn(tokens: GitHubTokens): String? {
        val name = identify(tokens)
        // Which account this is stays unknown, so a saved registration cannot be shown beside it. It can be read again.
        if (name == null) store.locked { registration.forget() }
        return name
    }

    /**
     * Finds the account of [tokens] and, if the sign-in is still saved, saves its name and removes a saved registration
     * of another account, so that it is never shown beside this sign-in. Returns null when GitHub cannot say.
     */
    fun identify(tokens: GitHubTokens): String? {
        val name = try {
            GitHubAccount.nameIn(api.get("/user", tokens.accessToken))
        } catch (e: IOException) {
            null
        } ?: return null
        store.locked {
            if (store.load() is LoadResult.Found) {
                val registered = registration.loadAny()
                if (registered != null && !registered.account.equals(name, ignoreCase = true)) registration.forget()
                account.save(name)
            }
        }
        return name
    }

    /**
     * Saves [name] after a registration, or, when the person has signed out while it ran, removes what the registration
     * left behind. [signInExpected] is false when the sign-in could not be kept, so its absence says nothing. Returns
     * false when the person has signed out.
     */
    fun afterRegistration(name: String, signInExpected: Boolean): Boolean = store.locked {
        if (signInExpected && store.load() !is LoadResult.Found) {
            registration.forget()
            false
        } else {
            account.save(name)
            true
        }
    }

    /** Removes what is saved for this environment, and creates nothing when nothing is saved. */
    fun signOut(): SignOutResult {
        if (!store.hasSavedFiles() && !account.exists() && !registration.exists()) return SignOutResult(false, emptyList())
        return store.locked {
            val remaining = buildList {
                if (!store.forget()) add("sign-in")
                if (!store.forgetWaiting()) add("waiting code")
                if (!account.forget()) add("account name")
                if (!registration.forget()) add("registration")
            }
            SignOutResult(true, remaining)
        }
    }
}

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
