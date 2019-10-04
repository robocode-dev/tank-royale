package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.model.GameSetup
import dev.robocode.tankroyale.ui.desktop.settings.GamesSettings
import dev.robocode.tankroyale.ui.desktop.settings.MutableGameSetup
import dev.robocode.tankroyale.ui.desktop.util.Event
import javax.swing.JComboBox

class GameTypeComboBox : JComboBox<String>(GamesSettings.games.keys.toTypedArray()) {

    val onGameTypeChanged = Event<String>()

    var gameSetup: GameSetup
        get() = (GamesSettings.games[selectedGameType] as MutableGameSetup).toGameSetup()
        set(value) {
            GamesSettings.games[selectedGameType] = value.toMutableGameSetup()
        }

    private val selectedGameType: String
        get() = selectedItem as String

    init {
        addActionListener { notifyGameTypeChanged() }
    }

    fun resetGameType() {
        val default: MutableGameSetup? = GamesSettings.defaultGameSetup[selectedGameType]
        if (default != null) {
            GamesSettings.games[selectedGameType] = default.copy()
            notifyGameTypeChanged()
        }
    }

    private fun notifyGameTypeChanged() {
        onGameTypeChanged.publish(selectedGameType)
    }
}