package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

interface IClientWebSocketObserver {
    fun onOpen(clientSocket: WebSocket, handshake: ClientHandshake)

    fun onClose(clientSocket: WebSocket, code: Int, reason: String, remote: Boolean)

    fun onMessage(clientSocket: WebSocket, message: String)

    fun onError(clientSocket: WebSocket?, exception: Exception)

    fun send(clientSocket: WebSocket, message: String)

    fun broadcast(clientSockets: Collection<WebSocket>, message: String)
}