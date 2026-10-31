package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Path

class RumbleEnvironmentTest {

    @Test
    @Tag("RCL-018")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL018_UnitPositive_exactRepositoriesBelongToTheirEnvironmentInAnyCommonForm() {
        val forms = listOf(
            "robocode-dev/rumble-bots-test",
            "ROBOCODE-DEV/Rumble-Bots-Test",
            "https://github.com/robocode-dev/rumble-bots-test",
            "https://github.com/robocode-dev/rumble-bots-test.git",
            "git@github.com:robocode-dev/rumble-bots-test.git",
            "ssh://git@github.com/robocode-dev/rumble-bots-test/",
            "  robocode-dev/rumble-bots-test  ",
        )
        forms.forEach { assertEquals(RumbleEnvironments.TEST, RumbleEnvironments.of(it), it) }

        assertEquals(RumbleEnvironments.TEST, RumbleEnvironments.of("robocode-dev/rumble-data-test"))
        assertEquals(RumbleEnvironments.PUBLIC, RumbleEnvironments.of("robocode-dev/rumble-bots"))
        assertEquals(RumbleEnvironments.PUBLIC, RumbleEnvironments.of("https://github.com/robocode-dev/rumble-data.git"))
    }

    @Test
    @Tag("RCL-018")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL018_UnitPositive_anInstallationThatNamesNoEnvironmentUsesThePublicBeta() {
        assertEquals(RumbleEnvironments.PUBLIC, RumbleEnvironments.DEFAULT)
        assertFalse(RumbleEnvironments.PUBLIC.development)
        assertFalse(RumbleEnvironments.PUBLIC.resettable)
        assertTrue(RumbleEnvironments.TEST.development)
        assertEquals("https://robocode-dev.github.io/rumble-data/", RumbleEnvironments.PUBLIC.dashboard)
        assertEquals("https://robocode-dev.github.io/rumble-data-test/", RumbleEnvironments.TEST.dashboard)
    }

    @Test
    @Tag("RCL-018")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL018_UnitNegative_lookalikeForeignAndUnknownRepositoriesBelongToNoEnvironment() {
        val outsiders = listOf(
            "robocode-dev/rumble-bots-test2",
            "robocode-dev/rumble-bots-tes",
            "robocode-dev/rumble-bots-",
            "robocode-dev/xrumble-bots",
            "robocode-dev2/rumble-bots",
            "someone-else/rumble-bots-test",
            "https://github.com/someone-else/rumble-data.git",
            "robocode-dev/tank-royale",
        )
        outsiders.forEach { assertNull(RumbleEnvironments.of(it), it) }
    }

    @Test
    @Tag("RCL-018")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL018_UnitNegative_aReferenceThatIsNotARepositoryIsRefused() {
        listOf("", "rumble-bots", "https://example.org/robocode-dev/rumble-bots", "a/b/c").forEach {
            assertThrows(EnvironmentRefused::class.java, { RumbleEnvironments.of(it) }, it)
        }
    }

    @Test
    @Tag("RCL-019")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL019_UnitPositive_eachEnvironmentGetsItsOwnStateFolderAndItsOwnQueue() {
        val base = Path.of("rumble-state")
        val publicRoot = RumbleEnvironments.PUBLIC.stateRoot(base)
        val testRoot = RumbleEnvironments.TEST.stateRoot(base)

        assertNotEquals(publicRoot, testRoot)
        assertFalse(publicRoot.startsWith(testRoot))
        assertFalse(testRoot.startsWith(publicRoot))
        assertTrue(publicRoot.startsWith(base))
        assertTrue(testRoot.startsWith(base))

        val queue = PendingQueue(RumbleEnvironments.TEST, listOf("a"))
        queue.add("b")
        assertEquals(listOf("a", "b"), queue.itemsFor(RumbleEnvironments.TEST))
    }

    @Test
    @Tag("RCL-019")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL019_UnitNegative_aQueueIsNeverHandedToAnotherEnvironment() {
        val queue = PendingQueue(RumbleEnvironments.TEST, listOf("a"))

        val refusal = assertThrows(EnvironmentRefused::class.java) { queue.itemsFor(RumbleEnvironments.PUBLIC) }

        assertTrue(refusal.message!!.contains("Development"))
        assertEquals(RumbleEnvironments.TEST, queue.environment)
        assertEquals(listOf("a"), queue.itemsFor(RumbleEnvironments.TEST))
    }
}
