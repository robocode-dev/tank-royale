package dev.robocode.tankroyale.client

import dev.robocode.tankroyale.common.Event

object WebSocketClientEvents {
    val onOpen = Event<Unit>()
    val onClose = Event<Unit>()
    val onMessage = Event<String>()
    val onError = Event<Throwable>()
}
