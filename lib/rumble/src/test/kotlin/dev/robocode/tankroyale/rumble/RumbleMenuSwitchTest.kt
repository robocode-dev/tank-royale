package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RumbleMenuSwitchTest {

    // RCL-055

    @Test
    @Tag("RCL-055")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL055_UnitPositive_theMenuIsShownForATrueWordInThePropertyOrTheVariable() {
        listOf("1", "true", "TRUE", " yes ", "on").forEach { word ->
            assertTrue(RumbleMenuSwitch.menuShown(word, null), word)
            assertTrue(RumbleMenuSwitch.menuShown(null, word), word)
        }
        assertTrue(RumbleMenuSwitch.menuShown("no", "true"))
    }

    @Test
    @Tag("RCL-055")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL055_UnitNegative_anotherWordOrNothingKeepsTheMenuHidden() {
        listOf("0", "false", "", " ", "maybe", "2").forEach { word ->
            assertFalse(RumbleMenuSwitch.menuShown(word, word), word)
        }
        assertFalse(RumbleMenuSwitch.menuShown(null, null))
    }

    @Test
    @Tag("RCL-055")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL055_UnitPositive_theEnvironmentIsTheNamedOneAndThePublicRumbleWhenNoneIsNamed() {
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.TEST), RumbleMenuSwitch.environment("test"))
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.TEST), RumbleMenuSwitch.environment(" test "))
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.PUBLIC), RumbleMenuSwitch.environment(null))
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.PUBLIC), RumbleMenuSwitch.environment("  "))
        assertEquals("RUMBLE_ENVIRONMENT", RumbleMenuSwitch.ENVIRONMENT_VARIABLE)
    }

    @Test
    @Tag("RCL-055")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL055_UnitNegative_anUnknownEnvironmentNameIsReportedAndNeverReplaced() {
        assertEquals(EnvironmentChoice.Unknown("nowhere"), RumbleMenuSwitch.environment("nowhere"))
        assertEquals(EnvironmentChoice.Unknown("Test"), RumbleMenuSwitch.environment("Test"), "names are exact, as on the command line")
    }
}
