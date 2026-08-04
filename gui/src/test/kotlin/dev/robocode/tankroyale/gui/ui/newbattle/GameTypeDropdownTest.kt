package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.GamesSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import javax.swing.SwingUtilities

class GameTypeDropdownTest {
    @Test
    @Tag("GTD-001")
    @Tag("Unit")
    @Tag("Positive")
    fun testGTD001_UnitPositive_twinDuelIsSelectableWithTheCommonPreset() {
        var offeredGameTypes = emptyList<String>()
        var selectedGameType: GameType? = null

        SwingUtilities.invokeAndWait {
            val dropdown = GameTypeDropdown()
            offeredGameTypes = (0 until dropdown.itemCount).map(dropdown::getItemAt)
            dropdown.setSelectedGameType(GameType.TWIN_DUEL)
            selectedGameType = dropdown.getSelectedGameType()
        }

        assertTrue("twinduel" in offeredGameTypes)
        assertEquals(GameType.TWIN_DUEL, selectedGameType)
        val twinDuelSetup = GamesSettings.games["twinduel"]
            ?: error("TwinDuel must have a game setup")
        assertEquals(800, twinDuelSetup.arenaWidth)
        assertEquals(800, twinDuelSetup.arenaHeight)
        assertEquals(4, twinDuelSetup.minNumberOfParticipants)
        assertEquals(4, twinDuelSetup.maxNumberOfParticipants)
        assertEquals(75, twinDuelSetup.numberOfRounds)
    }

    @Test
    @Tag("GTD-001")
    @Tag("Unit")
    @Tag("Negative")
    fun testGTD001_UnitNegative_nonCanonicalTwinDuelNameIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GameType.from("twin-duel")
        }
    }
}
