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

    private fun member(name: String, team: String, teamId: Int, port: Int) = dev.robocode.tankroyale.client.model.BotInfo(
        name = name, version = "1.0.0", authors = listOf("A"), countryCodes = emptyList(), gameTypes = setOf("twinduel"),
        teamId = teamId, teamName = team, teamVersion = "1.0.0", host = "localhost", port = port,
    )

    private fun twinDuelStartable(bots: List<dev.robocode.tankroyale.client.model.BotInfo>): Boolean {
        val setup = GamesSettings.games["twinduel"] ?: error("TwinDuel must have a game setup")
        return BattleParticipants.startable(bots, setup.minNumberOfParticipants, setup.maxNumberOfParticipants)
    }

    @Test
    @Tag("GTD-002")
    @Tag("Unit")
    @Tag("Positive")
    fun testGTD002_UnitPositive_twoTeamsOfTwoBotsFillTheFourTwinDuelPlaces() {
        val teams = listOf(
            member("Corners", "CornersCrazy", 1, 1), member("Crazy", "CornersCrazy", 1, 2),
            member("Retcon", "RetconTwins", 2, 3), member("Retcon", "RetconTwins", 2, 4),
        )

        assertEquals(4, BattleParticipants.count(teams))
        assertTrue(twinDuelStartable(teams))
    }

    @Test
    @Tag("GTD-002")
    @Tag("Unit")
    @Tag("Negative")
    fun testGTD002_UnitNegative_fewerOrMoreThanFourBotsCannotStartATwinDuel() {
        val two = listOf(member("Corners", "CornersCrazy", 1, 1), member("Crazy", "CornersCrazy", 1, 2))
        val six = two + listOf(
            member("Retcon", "RetconTwins", 2, 3), member("Retcon", "RetconTwins", 2, 4),
            member("Orbit", "OrbitVector", 3, 5), member("Vector", "OrbitVector", 3, 6),
        )

        org.junit.jupiter.api.Assertions.assertFalse(twinDuelStartable(two))
        org.junit.jupiter.api.Assertions.assertFalse(twinDuelStartable(six))
    }
}
