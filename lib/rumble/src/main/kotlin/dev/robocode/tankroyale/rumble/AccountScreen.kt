package dev.robocode.tankroyale.rumble

import java.time.Instant
import java.util.concurrent.Executor

/** Why the account screen shows "signed out". The screen turns each one into a plain sentence of its own. */
enum class AccountNotice { CODE_EXPIRED, CANCELLED, DENIED, SAVED_UNUSABLE, NOT_PRIVATE, NOT_SAVED, REVOKED, FAILED, SIGN_OUT_FAILED }

/** How registering the signed-in account with the Rumble stands. Never holds a token. */
sealed interface AccountRegistration {
    data object Registering : AccountRegistration
    data class Registered(val clientId: String) : AccountRegistration
    data class Refused(val reason: String) : AccountRegistration
    data object NotFinished : AccountRegistration
    data class Failed(val message: String) : AccountRegistration

    /** GitHub did not accept the sign-in while registering, so the person must sign in again. */
    data class SignInNeeded(val message: String) : AccountRegistration
}

/** What the account screen shows. A state never holds a token; only the short code is ever shown. */
sealed interface AccountState {
    data object Starting : AccountState

    /** Not signed in. [notice] says why when there is a reason, and [detail] is the library's plain message, if any. */
    data class SignedOut(val notice: AccountNotice? = null, val detail: String? = null) : AccountState

    /** This Rumble has no sign-in App, so there is nothing to press. [message] is the library's plain message. */
    data class Unavailable(val message: String) : AccountState

    data class WaitingForCode(val code: String, val address: String, val expiresAt: Instant) : AccountState

    data class SignedIn(val account: String?, val registration: AccountRegistration) : AccountState
}

/** The ways the account screen's library can answer. All of them are plain data. */
sealed interface AccountOpened {
    data class SignedIn(val account: String?) : AccountOpened
    data class SignedOut(val notice: AccountNotice? = null, val detail: String? = null) : AccountOpened
    data class Unavailable(val message: String) : AccountOpened
    data class Failed(val message: String) : AccountOpened
}

sealed interface AccountBegun {
    data class Code(val code: String, val address: String, val expiresAt: Instant, val retryAfterSeconds: Long) : AccountBegun
    data class AlreadySignedIn(val account: String?) : AccountBegun
    data class Unavailable(val message: String) : AccountBegun
    data class NotPrivate(val message: String) : AccountBegun
    data class Failed(val message: String) : AccountBegun
}

sealed interface AccountChecked {
    data class Waiting(val code: String, val address: String, val expiresAt: Instant, val retryAfterSeconds: Long) : AccountChecked
    data class SignedIn(val account: String?) : AccountChecked
    data object Expired : AccountChecked
    data object Denied : AccountChecked
    data object NoWaitingCode : AccountChecked
    data class NotSaved(val reason: String) : AccountChecked
    data class Unavailable(val message: String) : AccountChecked

    /** The check failed. When [canRetry] is true checking again after [retryAfterSeconds] can still work. */
    data class Failed(val message: String, val canRetry: Boolean, val retryAfterSeconds: Long = 0) : AccountChecked
}

/**
 * What the account screen needs of the Rumble library. Every call may take a while and may block, so the screen's
 * behavior only calls them away from the screen's own thread. A test stands in for the library with a fake.
 */
interface AccountPort {
    /** Renews what is saved, and says whether the person is signed in. */
    fun open(): AccountOpened

    /** Asks GitHub for a code, or reuses the one that is waiting. */
    fun begin(): AccountBegun

    /** Checks once whether the person has approved the code. */
    fun check(): AccountChecked

    /** Registers the signed-in account with the Rumble. */
    fun register(): AccountRegistration

    /** Forgets a waiting code and keeps anything else that is saved. */
    fun cancel()

    /** Removes what is saved. Returns false when something could not be removed. */
    fun signOut(): Boolean
}

/** Runs a task after a number of seconds, and lets it be cancelled. A test moves time by hand. */
fun interface AccountScheduler {
    fun after(seconds: Long, task: () -> Unit): AutoCloseable
}

/**
 * The behavior of the GitHub account screen, without any window. The screen shows [state] whenever [onState] is called
 * and passes its button presses to [open], [signIn], [cancel], [close] and [signOut], each of which returns at once.
 *
 * All work that talks to GitHub or the disk runs on [background]. All changes of state, and every call of [onState],
 * run on [post], one at a time and in order, which in the desktop program is the screen's event queue. A result that
 * arrives after the person has cancelled, closed or signed out is thrown away. The code is checked again only after
 * the pace GitHub asked for, and a check that failed but can still work is tried again.
 */
class AccountController(
    private val port: AccountPort,
    private val background: Executor,
    private val post: Executor,
    private val scheduler: AccountScheduler,
    private val onState: (AccountState) -> Unit,
) {
    // Everything below is touched only on [post].
    private var epoch = 0
    private var scheduled: AutoCloseable? = null
    private var asking = false

    @Volatile
    var state: AccountState = AccountState.Starting
        private set

    /** Looks at what is saved. Call when the screen opens. */
    fun open() = post.execute {
        stopWaiting()
        show(AccountState.Starting)
        val mine = epoch
        work({ port.open() }, { AccountOpened.Failed(NO_DETAIL) }) { opened ->
            if (mine == epoch) when (opened) {
                is AccountOpened.SignedIn -> signedIn(opened.account)
                is AccountOpened.SignedOut -> show(AccountState.SignedOut(opened.notice, opened.detail?.takeIf { it.isNotBlank() }))
                is AccountOpened.Unavailable -> show(AccountState.Unavailable(opened.message))
                is AccountOpened.Failed -> show(AccountState.SignedOut(AccountNotice.FAILED, opened.message.takeIf { it.isNotBlank() }))
            }
        }
    }

    /** Starts signing in. Does nothing unless the person is signed out. */
    fun signIn() = post.execute {
        if (state !is AccountState.SignedOut || asking) return@execute
        asking = true
        epoch++
        val mine = epoch
        work({ port.begin() }, { AccountBegun.Failed(NO_DETAIL) }) { begun ->
            asking = false
            if (mine != epoch) return@work
            when (begun) {
                is AccountBegun.Code -> {
                    show(AccountState.WaitingForCode(begun.code, begun.address, begun.expiresAt))
                    checkAfter(begun.retryAfterSeconds, mine)
                }
                is AccountBegun.AlreadySignedIn -> signedIn(begun.account)
                is AccountBegun.Unavailable -> show(AccountState.Unavailable(begun.message))
                is AccountBegun.NotPrivate -> show(AccountState.SignedOut(AccountNotice.NOT_PRIVATE, begun.message.takeIf { it.isNotBlank() }))
                is AccountBegun.Failed -> show(AccountState.SignedOut(AccountNotice.FAILED, begun.message.takeIf { it.isNotBlank() }))
            }
        }
    }

    /** The person cancels the sign-in: the waiting code is forgotten and no more checks are made. */
    fun cancel() = post.execute {
        val waiting = state is AccountState.WaitingForCode
        stopWaiting()
        if (waiting) show(AccountState.SignedOut(AccountNotice.CANCELLED))
        background.execute { runCatching { port.cancel() } }
    }

    /** The person closes the screen: no more checks are made, and a waiting code is kept for the next time. */
    fun close() = post.execute {
        val waiting = state is AccountState.WaitingForCode
        stopWaiting()
        if (waiting) show(AccountState.SignedOut())
    }

    /** Removes the saved sign-in. */
    fun signOut() = post.execute {
        stopWaiting()
        val mine = epoch
        work({ port.signOut() }, { false }) { removed ->
            if (mine == epoch) show(if (removed) AccountState.SignedOut() else AccountState.SignedOut(AccountNotice.SIGN_OUT_FAILED))
        }
    }

    private fun checkAfter(seconds: Long, mine: Int) {
        scheduled?.close()
        scheduled = scheduler.after(seconds.coerceAtLeast(0)) {
            post.execute {
                if (mine != epoch) return@execute
                work({ port.check() }, { AccountChecked.Failed(NO_DETAIL, canRetry = false) }) { checked ->
                    if (mine == epoch) applyCheck(checked, mine)
                }
            }
        }
    }

    private fun applyCheck(checked: AccountChecked, mine: Int) {
        when (checked) {
            is AccountChecked.Waiting -> {
                show(AccountState.WaitingForCode(checked.code, checked.address, checked.expiresAt))
                checkAfter(checked.retryAfterSeconds, mine)
            }
            is AccountChecked.SignedIn -> signedIn(checked.account)
            AccountChecked.Expired -> show(AccountState.SignedOut(AccountNotice.CODE_EXPIRED))
            AccountChecked.Denied -> show(AccountState.SignedOut(AccountNotice.DENIED))
            AccountChecked.NoWaitingCode -> show(AccountState.SignedOut())
            is AccountChecked.NotSaved -> show(AccountState.SignedOut(AccountNotice.NOT_SAVED, checked.reason.takeIf { it.isNotBlank() }))
            is AccountChecked.Unavailable -> show(AccountState.Unavailable(checked.message))
            is AccountChecked.Failed ->
                if (checked.canRetry && state is AccountState.WaitingForCode) checkAfter(checked.retryAfterSeconds, mine)
                else show(AccountState.SignedOut(AccountNotice.FAILED, checked.message.takeIf { it.isNotBlank() }))
        }
    }

    /** Shows signed in at once, and registers the account away from the screen's thread. */
    private fun signedIn(account: String?) {
        show(AccountState.SignedIn(account, AccountRegistration.Registering))
        val mine = epoch
        work({ port.register() }, { AccountRegistration.Failed(NO_DETAIL) }) { registration ->
            if (mine != epoch) return@work
            val now = state
            when {
                registration is AccountRegistration.SignInNeeded -> show(AccountState.SignedOut(AccountNotice.REVOKED, registration.message.takeIf { it.isNotBlank() }))
                now is AccountState.SignedIn -> show(now.copy(registration = registration))
            }
        }
    }

    /** Stops checking and makes every result that is still on its way useless. */
    private fun stopWaiting() {
        epoch++
        asking = false
        scheduled?.close()
        scheduled = null
    }

    private fun show(next: AccountState) {
        state = next
        onState(next)
    }

    /**
     * Runs [call] on [background], and its result, or [fallback] when it throws anything, also an error that is not an
     * exception, on [post]; what was thrown is written to the error output. The screen never waits for an answer that
     * will not come.
     */
    private fun <T> work(call: () -> T, fallback: () -> T, then: (T) -> Unit) {
        background.execute {
            val result = attempt(call).getOrElse { fallback() }
            post.execute { then(result) }
        }
    }

    private companion object {
        /** The detail of a failure that has none. The screen shows no detail line for it and says what happened in its own words. */
        const val NO_DETAIL = ""
    }
}
