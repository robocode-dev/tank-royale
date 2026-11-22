package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/** Where the Get started screen's buttons lead, with and without a saved sign-in, without a window. */
class RumbleGetStartedTest {

    @TempDir
    lateinit var dir: Path

    @Test
    @Tag("RCL-142")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL142_UnitPositive_withASavedSignInEachButtonLeadsToItsOwnPlaceOrScreen() {
        assertEquals(GetStartedDestination.Page("https://robocode.dev/tutorial/my-first-bot"), GetStarted.destination(GetStartedButton.MAKE_BOT, savedSignIn = true))
        assertEquals(GetStartedDestination.Page("https://github.com/robocode-dev/tank-royale/issues/new?template=rumble_feedback.yml"), GetStarted.destination(GetStartedButton.FEEDBACK, savedSignIn = true))
        assertEquals(GetStartedDestination.SubmitScreen, GetStarted.destination(GetStartedButton.SUBMIT_BOT, savedSignIn = true))
        assertEquals(GetStartedDestination.ContributeScreen, GetStarted.destination(GetStartedButton.CONTRIBUTE, savedSignIn = true))
    }

    @Test
    @Tag("RCL-142")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL142_UnitNegative_withoutASavedSignInSubmittingAndContributingLeadToTheAccountScreenButThePagesDoNot() {
        assertEquals(GetStartedDestination.AccountScreen, GetStarted.destination(GetStartedButton.SUBMIT_BOT, savedSignIn = false))
        assertEquals(GetStartedDestination.AccountScreen, GetStarted.destination(GetStartedButton.CONTRIBUTE, savedSignIn = false))
        assertEquals(GetStarted.destination(GetStartedButton.MAKE_BOT, true), GetStarted.destination(GetStartedButton.MAKE_BOT, false))
        assertEquals(GetStarted.destination(GetStartedButton.FEEDBACK, true), GetStarted.destination(GetStartedButton.FEEDBACK, false))
    }

    @Test
    @Tag("RCL-142")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL142_UnitPositive_aSavedSignInIsSeenOnlyForItsOwnEnvironment() {
        val test = SignInStore(RumbleEnvironments.TEST, dir)
        val public = SignInStore(RumbleEnvironments.PUBLIC, dir)
        assertFalse(GetStarted.hasSavedSignIn(test))
        assertEquals(SaveResult.Saved, test.save("ghr_SAVED", null))
        assertTrue(GetStarted.hasSavedSignIn(test))
        assertFalse(GetStarted.hasSavedSignIn(public), "a sign-in for the test Rumble is not one for the public Rumble")
        assertTrue(test.forget())
        assertFalse(GetStarted.hasSavedSignIn(test), "signing out removes it")
    }
}
