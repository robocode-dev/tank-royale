package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/** The desktop program's Rumble environment, and the Rumble menu that is always shown. */
class RumbleMenuSwitchTest {

    /** The source of a file of the repository, read from the root or from a module's folder. */
    private fun code(path: String): String =
        listOf(Path.of(path), Path.of("..", "..", path), Path.of("..", path)).first { Files.exists(it) }.let { Files.readString(it) }

    // RCL-168

    @Test
    @Tag("RCL-168")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL168_UnitPositive_theEnvironmentIsTheNamedOneAndThePublicRumbleWhenNoneIsNamed() {
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.TEST), RumbleMenuSwitch.environment("test"))
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.TEST), RumbleMenuSwitch.environment(" test "))
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.PUBLIC), RumbleMenuSwitch.environment(null))
        assertEquals(EnvironmentChoice.Chosen(RumbleEnvironments.PUBLIC), RumbleMenuSwitch.environment("  "))
        assertEquals("RUMBLE_ENVIRONMENT", RumbleMenuSwitch.ENVIRONMENT_VARIABLE)
    }

    @Test
    @Tag("RCL-168")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL168_UnitNegative_noSwitchHidesTheMenuOrTheSessionLookupAndAnUnknownEnvironmentIsReported() {
        assertEquals(EnvironmentChoice.Unknown("nowhere"), RumbleMenuSwitch.environment("nowhere"))
        assertEquals(EnvironmentChoice.Unknown("Test"), RumbleMenuSwitch.environment("Test"), "names are exact, as on the command line")

        // The menu bar and the session lookup at start read no switch any more.
        val menu = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/menu/Menu.kt")
        val contribute = code("gui/src/main/kotlin/dev/robocode/tankroyale/gui/ui/rumble/RumbleContribute.kt")
        for (source in listOf(menu, contribute)) {
            assertFalse(source.contains("RUMBLE_MENU") || source.contains("rumble.menu") || source.contains("menuShown"))
        }
        // At start the program follows a session only where one has run, so a person who never used the Rumble gets nothing.
        assertFalse(SessionLook.None.worthFollowing)
        assertTrue(SessionLook.Running(null).worthFollowing)
        val startUp = contribute.substringAfter("fun startUp()").substringBefore("private fun watch(")
        val look = startUp.indexOf("folder.look()")
        val follow = startUp.indexOf("controllerFor(")
        assertTrue(look >= 0 && follow >= 0 && look < follow && startUp.contains("worthFollowing"), "the controller is made only after the look")
        assertTrue(startUp.contains("else watch("), "without a session the program still watches the folder")
    }
}
