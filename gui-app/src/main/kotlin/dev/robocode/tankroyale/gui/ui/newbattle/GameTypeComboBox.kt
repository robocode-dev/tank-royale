package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.GamesSettings
import javax.swing.JComboBox

class GameTypeComboBox : JComboBox<String>(GamesSettings.games.keys.toTypedArray()) {

    init {
        setSelectedGameType(ConfigSettings.gameType)

        ConfigSettings.onSaved.subscribe(this) {
            setSelectedGameType(ConfigSettings.gameType)
        }
    }

    fun getSelectedGameType(): GameType =
        if (model.selectedItem == null) {
            GameType.CLASSIC
        } else {
            GameType.from(model.selectedItem as String)
        }

    fun setSelectedGameType(gameType: GameType) {
        model.selectedItem = gameType.displayName
    }
}