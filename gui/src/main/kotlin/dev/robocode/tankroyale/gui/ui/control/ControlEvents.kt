package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.common.event
import javax.swing.JButton

object ControlEvents {
    val onStop by event<JButton>()
    val onRestart by event<JButton>()
    val onPauseResume by event<JButton>()
    val onNextTurn by event<JButton>()
}
