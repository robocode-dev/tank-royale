package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.util.Event
import javax.swing.JButton

object ControlEvents {
    val onStop = Event<JButton>()
    val onRestart = Event<JButton>()
    val onPauseResume = Event<JButton>()
    val onNextTurn = Event<JButton>()
}