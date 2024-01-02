package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.GameType.*
import dev.robocode.tankroyale.gui.ui.components.RcComboBox

class GameTypeDropdown : RcComboBox<String>(
    listOf(CLASSIC, ONE_VS_ONE, MELEE, CUSTOM).map { it.displayName }.toTypedArray()
) { // setup in specific order

    init {
        setSelectedGameType(ConfigSettings.gameType)

        ConfigSettings.onSaved.subscribe(this) {
            setSelectedGameType(ConfigSettings.gameType)
        }
    }

    fun getSelectedGameType(): GameType =
        if (model.selectedItem == null) {
            CLASSIC
        } else {
            GameType.from(model.selectedItem as String)
        }

    fun setSelectedGameType(gameType: GameType) {
        model.selectedItem = gameType.displayName
    }
}