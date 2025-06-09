package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.common.Event

object ServerEventTriggers {
    val onStartLocalServer = Event<Unit>()
    val onStopLocalServer = Event<Unit>()
    val onRebootLocalServer = Event<Boolean /* true, when due to setting change */>()
}