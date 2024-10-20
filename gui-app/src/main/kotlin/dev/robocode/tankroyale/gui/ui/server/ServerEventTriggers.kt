package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.util.Event

object ServerEventTriggers {
    val onStartLocalServer = Event<Unit>()
    val onStopLocalServer = Event<Unit>()
    val onRebootLocalServer = Event<Boolean /* true, when due to setting change */>()
}