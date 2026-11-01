package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class RumbleRegistrationLiveTest {

    /**
     * Reads the real registration file of the test account in the real test result repository, with no token, and asks
     * for an account that has none. It only reads. Run with -Drumble.live=true.
     */
    @Test
    @Tag("Sanity")
    fun testSanity_theRealRegistrationFileIsReadAndAnUnknownAccountHasNone(@TempDir base: Path) {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to contact GitHub")
        val env = RumbleEnvironments.TEST
        val registration = Registration(env, HttpGitHubApi(), RegistrationStore(env, base))

        val clientId = registration.readRegistrationFile("robocode-rumble-test", null)

        assertTrue(Regex("robocode-rumble-test-[0-9a-f]{8}").matches(clientId ?: ""), "Unexpected client ID: $clientId")
        assertNull(registration.readRegistrationFile("no-such-rumble-account-xyz", null))
        println("LIVE registration file read: $clientId")
    }
}
