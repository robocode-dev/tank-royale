package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.gui.util.Event

object WebSocketClientEvents {
    val onOpen = Event<Unit>()
    val onClose = Event<Unit>()
    val onMessage = Event<String>()
    val onError = Event<Throwable>()
}