package dev.robocode.tankroyale.client

object WebSocketClientEvents {
    val onOpen = Event<Unit>()
    val onClose = Event<Unit>()
    val onMessage = Event<String>()
    val onError = Event<Throwable>()
}
