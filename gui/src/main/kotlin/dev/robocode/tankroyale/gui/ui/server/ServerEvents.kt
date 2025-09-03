package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.common.Event

object ServerEvents {
    val onConnected = Event<Unit>()
    val onStarted = Event<Unit>()
    val onStopped = Event<Unit>()
}