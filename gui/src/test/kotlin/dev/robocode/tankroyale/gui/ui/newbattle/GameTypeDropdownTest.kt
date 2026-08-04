package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.GamesSettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import javax.swing.SwingUtilities

class GameTypeDropdownTest : StringSpec({
    "testGTD001_UnitPositive_twinDuelIsSelectableWithTheCommonPreset" {
        var offeredGameTypes = emptyList<String>()
        var selectedGameType: GameType? = null

        SwingUtilities.invokeAndWait {
            val dropdown = GameTypeDropdown()
            offeredGameTypes = (0 until dropdown.itemCount).map(dropdown::getItemAt)
            dropdown.setSelectedGameType(GameType.TWIN_DUEL)
            selectedGameType = dropdown.getSelectedGameType()
        }

        offeredGameTypes shouldContain "twinduel"
        selectedGameType shouldBe GameType.TWIN_DUEL
        val twinDuelSetup = GamesSettings.games["twinduel"]
            ?: error("TwinDuel must have a game setup")
        twinDuelSetup.apply {
            arenaWidth shouldBe 800
            arenaHeight shouldBe 800
            minNumberOfParticipants shouldBe 4
            maxNumberOfParticipants shouldBe 4
            numberOfRounds shouldBe 75
        }
    }

    "testGTD001_UnitNegative_nonCanonicalTwinDuelNameIsRejected" {
        shouldThrow<IllegalArgumentException> {
            GameType.from("twin-duel")
        }
    }
})
