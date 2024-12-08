package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.server.Server
import org.java_websocket.WebSocket
import java.net.InetAddress
import java.net.InetSocketAddress

class MultiServerWebSocketObserver(observer: IClientWebSocketObserver) {

    private val webSocketServer = if (Server.useInheritedChannel) {
        arrayOf(ServerWebSocketObserver(observer))
    } else {
        arrayOf(
            ServerWebSocketObserver(InetSocketAddress(Server.port), observer),
            ServerWebSocketObserver(InetSocketAddress(InetAddress.getLocalHost(), Server.port), observer)
        )
    }

    fun start() {
        webSocketServer.forEach { it.run() }
    }

    fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        webSocketServer.forEach { it.broadcast(message, clientSockets) }
    }
}