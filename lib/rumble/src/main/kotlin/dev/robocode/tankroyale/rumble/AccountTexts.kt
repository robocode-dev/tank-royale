package dev.robocode.tankroyale.rumble

/**
 * The keys of the texts the account screen shows. The texts themselves are in the desktop program's resource files, in
 * English first; this object is the one list of which keys a state, a notice or a registration result needs, so a test
 * can check that every one of them has a text. Nothing here says anything about tokens, because nothing in a state
 * holds one.
 */
object AccountTexts {
    /** The key of the heading text of [state], in the messages file. */
    fun forState(state: AccountState): String = when (state) {
        AccountState.Starting -> "rumble.account.starting"
        is AccountState.SignedOut -> "rumble.account.signed_out"
        is AccountState.Unavailable -> "rumble.account.unavailable"
        is AccountState.WaitingForCode -> "rumble.account.waiting"
        is AccountState.SignedIn -> "rumble.account.signed_in"
    }

    /** The key of the sentence that says why the person is signed out. */
    fun forNotice(notice: AccountNotice): String = "rumble.account.notice." + notice.name.lowercase()

    /** The key of the sentence about the registration. */
    fun forRegistration(registration: AccountRegistration): String = when (registration) {
        AccountRegistration.Registering -> "rumble.account.registering"
        is AccountRegistration.Registered -> "rumble.account.registered"
        is AccountRegistration.Refused -> "rumble.account.refused"
        AccountRegistration.NotFinished -> "rumble.account.not_finished"
        is AccountRegistration.Failed -> "rumble.account.registration_failed"
        is AccountRegistration.SignInNeeded -> "rumble.account.notice.revoked"
    }

    /** The keys that are not about a state: the text of the waiting line, the signed-in line without a name and the buttons. */
    val OTHER: List<String> = listOf(
        "rumble.account.waiting_expires", "rumble.account.signed_in_unknown",
        "rumble.account.button.sign_in", "rumble.account.button.cancel", "rumble.account.button.sign_out",
        "rumble.account.button.open_page", "rumble.account.button.close", "rumble.account.step_open", "rumble.account.step_enter",
        "rumble.account.unknown_environment", "rumble.account.details", "rumble.account.button.copy_code",
        "rumble.account.no_account", "rumble.account.button.create_account",
    )

    /** The states, notices and registrations that the screen can show, for checking that each has a text. */
    val ALL_STATES: List<AccountState> = listOf(
        AccountState.Starting, AccountState.SignedOut(), AccountState.Unavailable(""),
        AccountState.WaitingForCode("", "", java.time.Instant.EPOCH), AccountState.SignedIn(null, AccountRegistration.Registering),
    )

    val ALL_REGISTRATIONS: List<AccountRegistration> = listOf(
        AccountRegistration.Registering, AccountRegistration.Registered(""), AccountRegistration.Refused(""),
        AccountRegistration.NotFinished, AccountRegistration.Failed(""), AccountRegistration.SignInNeeded(""),
    )

    /** Every key the account screen can ask for. */
    val ALL_KEYS: List<String> =
        (ALL_STATES.map { forState(it) } + AccountNotice.entries.map { forNotice(it) } + ALL_REGISTRATIONS.map { forRegistration(it) } + OTHER).distinct()
}
