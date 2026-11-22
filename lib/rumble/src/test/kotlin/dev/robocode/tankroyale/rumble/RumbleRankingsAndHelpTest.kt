package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/** The pages the Rumble menu's Rankings and Help open. */
class RumbleRankingsAndHelpTest {

    private fun rankingsFor(variable: String?): String? =
        RumbleLinks.rankings((RumbleMenuSwitch.environment(variable) as EnvironmentChoice.Chosen).environment)

    @Test
    @Tag("RCL-144")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL144_UnitPositive_rankingsOpensTheSelectedEnvironmentsDashboardAndHelpOpensTheGuide() {
        assertEquals("https://robocode-dev.github.io/rumble-data/", rankingsFor("public"))
        assertEquals("https://robocode-dev.github.io/rumble-data-test/", rankingsFor("test"), "only when a developer sets the development Rumble")
        assertEquals("https://robocode.dev/rumble/", RumbleLinks.GUIDE)
        assertTrue(RumbleLinks.isHttpsPage(RumbleLinks.GUIDE))
    }

    @Test
    @Tag("RCL-144")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL144_UnitNegative_withoutTestModeNoTestPageIsOpenedAndNoAddressThatIsNotAnHttpsPage() {
        // A program that no developer has set to the development Rumble opens the public rankings.
        for (unset in listOf(null, "", "   ")) assertEquals("https://robocode-dev.github.io/rumble-data/", rankingsFor(unset), "variable '$unset'")
        for (address in listOf("http://robocode-dev.github.io/rumble-data/", "file:///C:/rankings.html", "javascript:alert(1)", "https://user@example.com/", "https:///no-host", "not a page", "")) {
            assertFalse(RumbleLinks.isHttpsPage(address), address)
            assertNull(RumbleLinks.rankings(RumbleEnvironments.PUBLIC.copy(dashboard = address)), address)
        }
    }
}
