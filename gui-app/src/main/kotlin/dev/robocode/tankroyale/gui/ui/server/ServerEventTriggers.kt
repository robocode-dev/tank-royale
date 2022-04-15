package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.util.Event

object ServerEventTriggers {
    val onStartServer = Event<Unit>()
    val onStopServer = Event<Unit>()
    val onRebootServer = Event<Boolean /* true, when due to setting change */>()
}