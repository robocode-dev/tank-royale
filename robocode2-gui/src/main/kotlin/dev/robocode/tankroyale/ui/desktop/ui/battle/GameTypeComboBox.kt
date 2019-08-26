package dev.robocode.tankroyale.ui.desktop.ui.battle

import dev.robocode.tankroyale.ui.desktop.settings.GamesSettings
import dev.robocode.tankroyale.ui.desktop.settings.MutableGameSetup
import dev.robocode.tankroyale.ui.desktop.utils.Event
import javax.swing.JComboBox

class GameTypeComboBox : JComboBox<String>(GamesSettings.games.keys.toTypedArray()) {

    val onGameTypeChanged = Event<String>()

    var mutableGameSetup: MutableGameSetup
        get() = GamesSettings.games[selectedGameType] as MutableGameSetup
        set(value) {
            GamesSettings.games[selectedGameType] = value
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

    fun notifyGameTypeChanged() {
        onGameTypeChanged.publish(selectedGameType)
    }
}