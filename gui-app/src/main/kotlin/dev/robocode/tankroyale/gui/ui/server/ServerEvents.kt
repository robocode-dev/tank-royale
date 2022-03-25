package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.util.Event

object ServerEvents {
    val onConnected = Event<Unit>()
    val onStartServer = Event<Unit>()
    val onStopServer = Event<Unit>()
    val onRestartServer = Event<Unit>()
}