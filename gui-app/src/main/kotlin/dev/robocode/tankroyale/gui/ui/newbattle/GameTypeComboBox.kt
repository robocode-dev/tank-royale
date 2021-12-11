package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import javax.swing.JComboBox

object GameTypeComboBox : JComboBox<String>(GamesSettings.games.keys.toTypedArray()) {

    init {
        setSelectedGameType(ServerSettings.gameType)
    }

    val selectedGameType: GameType get() =
        if (model.selectedItem == null) {
            GameType.CLASSIC
        } else {
            GameType.from(model.selectedItem as String)
        }

    private fun setSelectedGameType(displayName: String) {
        model.selectedItem = displayName
    }

    fun setSelectedGameType(gameType: GameType) {
        setSelectedGameType(gameType.displayName)
    }
}