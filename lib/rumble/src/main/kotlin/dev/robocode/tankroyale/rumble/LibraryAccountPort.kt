package dev.robocode.tankroyale.rumble

import java.time.Clock

/**
 * The account screen's library, made of the same sign-in flow and registration that the command line uses, on the
 * same saved files. Every answer is plain data; a token is held only for the length of a call and never leaves it.
 */
class LibraryAccountPort(
    private val environment: RumbleEnvironment,
    private val flow: SignInFlow,
    private val api: GitHubApi,
    private val clock: Clock = Clock.systemUTC(),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
    private val registrationPollSeconds: Int = 5,
    private val registrationWaitSeconds: Int = 120,
) : AccountPort {

    override fun open(): AccountOpened {
        if (environment.signInClientId == null) return AccountOpened.Unavailable(environment.signInNotAvailable())
        return when (val renewal = flow.renewSaved()) {
            is SavedRenewal.Renewed -> AccountOpened.SignedIn(renewal.account ?: flow.identify(renewal))
            is SavedRenewal.SignInNeeded -> when (renewal.reason) {
                SavedRenewal.SignInNeeded.Reason.MISSING -> AccountOpened.SignedOut()
                SavedRenewal.SignInNeeded.Reason.UNUSABLE -> AccountOpened.SignedOut(AccountNotice.SAVED_UNUSABLE, renewal.message)
                SavedRenewal.SignInNeeded.Reason.REFUSED -> AccountOpened.SignedOut(AccountNotice.REVOKED, renewal.message)
            }
            is SavedRenewal.Failed -> AccountOpened.Failed(renewal.message)
        }
    }

    override fun begin(): AccountBegun =
        when (val start = flow.start()) {
            is SignInStart.Started -> AccountBegun.Code(start.code.userCode, start.code.verificationUri, start.code.expiresAt, start.code.intervalSeconds.toLong())
            is SignInStart.AlreadySignedIn -> AccountBegun.AlreadySignedIn(start.account)
            is SignInStart.Unavailable -> AccountBegun.Unavailable(start.message)
            is SignInStart.NotPrivate -> AccountBegun.NotPrivate(start.message)
            is SignInStart.Failed -> AccountBegun.Failed(start.message)
        }

    override fun check(): AccountChecked =
        when (val finish = flow.finish(0)) {
            is SignInFinish.SignedIn -> AccountChecked.SignedIn(finish.account)
            is SignInFinish.Waiting -> AccountChecked.Waiting(finish.code.userCode, finish.code.verificationUri, finish.code.expiresAt, finish.retryAfterSeconds)
            is SignInFinish.NotSaved -> AccountChecked.NotSaved(finish.reason)
            SignInFinish.NoWaitingCode -> AccountChecked.NoWaitingCode
            SignInFinish.CodeExpired -> AccountChecked.Expired
            SignInFinish.CodeDenied -> AccountChecked.Denied
            is SignInFinish.Unavailable -> AccountChecked.Unavailable(finish.message)
            is SignInFinish.Failed -> AccountChecked.Failed(finish.message, finish.codeStillWaiting, finish.retryAfterSeconds)
        }

    override fun register(): AccountRegistration {
        val renewal = when (val r = flow.renewSaved()) {
            is SavedRenewal.Renewed -> r
            is SavedRenewal.SignInNeeded -> return AccountRegistration.SignInNeeded(r.message)
            is SavedRenewal.Failed -> return AccountRegistration.Failed(r.message)
        }
        if (renewal.account == null) flow.identify(renewal)
        val registration = Registration(environment, api, flow.registrationMemory(renewal), clock, sleep, registrationPollSeconds, registrationWaitSeconds)
        return when (val result = registration.register(renewal.tokens)) {
            is RegistrationResult.Registered -> AccountRegistration.Registered(result.clientId)
            is RegistrationResult.Refused -> AccountRegistration.Refused(result.reason)
            is RegistrationResult.NotFinished -> AccountRegistration.NotFinished
            is RegistrationResult.SignInNeeded -> {
                flow.forgetRefused(renewal)
                AccountRegistration.SignInNeeded(result.message)
            }
            is RegistrationResult.Failed -> AccountRegistration.Failed(result.message)
        }
    }

    override fun cancel() = flow.cancelWaiting()

    override fun signOut(): Boolean = flow.signOut().removed
}
