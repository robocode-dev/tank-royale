package dev.robocode.tankroyale.rumble

/** A button on the Rumble's Get started screen. */
enum class GetStartedButton { MAKE_BOT, SUBMIT_BOT, CONTRIBUTE, FEEDBACK }

/** Where a button on the Get started screen leads. The screen only follows it; it decides nothing itself. */
sealed interface GetStartedDestination {
    /** A page the browser opens. */
    data class Page(val address: String) : GetStartedDestination

    /** The Submit or update bot screen. */
    data object SubmitScreen : GetStartedDestination

    /** The Contribute battles screen. */
    data object ContributeScreen : GetStartedDestination

    /** The GitHub account screen, so the person signs in before going on. */
    data object AccountScreen : GetStartedDestination
}

/**
 * The behaviour of the Get started screen: where each button leads. Submitting a bot and contributing battles need a
 * GitHub account, so without a saved sign-in on this computer those buttons lead to the GitHub account screen first.
 * Whether a saved sign-in still works is not checked here; the Submit and Contribute screens say so themselves and offer
 * the account screen, and this screen never waits for the network.
 */
object GetStarted {
    /** The "My first bot" tutorial, which explains making a bot in each of the official languages. */
    const val TUTORIAL_PAGE = "https://robocode.dev/tutorial/my-first-bot"

    /** The repository's issue form for feedback on the Rumble. */
    const val FEEDBACK_PAGE = "https://github.com/robocode-dev/tank-royale/issues/new?template=rumble_feedback.yml"

    fun destination(button: GetStartedButton, savedSignIn: Boolean): GetStartedDestination = when (button) {
        GetStartedButton.MAKE_BOT -> GetStartedDestination.Page(TUTORIAL_PAGE)
        GetStartedButton.FEEDBACK -> GetStartedDestination.Page(FEEDBACK_PAGE)
        GetStartedButton.SUBMIT_BOT -> if (savedSignIn) GetStartedDestination.SubmitScreen else GetStartedDestination.AccountScreen
        GetStartedButton.CONTRIBUTE -> if (savedSignIn) GetStartedDestination.ContributeScreen else GetStartedDestination.AccountScreen
    }

    /** True when [store] holds a saved sign-in. Only looks whether its file is there, so it is quick and safe on any thread. */
    fun hasSavedSignIn(store: SignInStore): Boolean = !store.knownMissing()
}
