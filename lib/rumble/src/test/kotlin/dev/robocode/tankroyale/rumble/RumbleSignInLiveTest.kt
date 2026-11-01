package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RumbleSignInLiveTest {

    /**
     * Asks GitHub for a real sign-in code for the development sign-in App and checks the answer's shape. It does not
     * wait for approval, so no person is needed. Run with -Drumble.live=true.
     */
    @Test
    @Tag("Sanity")
    fun testSanity_gitHubGivesARealSignInCodeWhenAskedTo() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to contact GitHub")

        val code = GitHubSignIn(RumbleEnvironments.TEST, HttpGitHubAuthTransport()).start()

        assertEquals("https://github.com/login/device", code.verificationUri)
        assertTrue(Regex("[A-Z0-9]{4}-[A-Z0-9]{4}").matches(code.userCode), "Unexpected user code shape: ${code.userCode}")
        assertTrue(code.intervalSeconds in 1..60, "Unexpected interval: ${code.intervalSeconds}")
        assertTrue(code.expiresAt.isAfter(java.time.Instant.now().plusSeconds(60)))
        println("LIVE sign-in code received: interval=${code.intervalSeconds}s, lasts until ${code.expiresAt}")
    }
}
