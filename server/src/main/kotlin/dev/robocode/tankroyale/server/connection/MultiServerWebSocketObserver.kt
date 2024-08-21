package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.server.Server
import org.java_websocket.WebSocket
import java.net.InetAddress
import java.net.InetSocketAddress

class MultiServerWebSocketObserver(observer: IClientWebSocketObserver) {

    private val loopbackServerWebSocketObserver = ServerWebSocketObserver(InetSocketAddress(Server.port), observer)
    private val localhostServerWebSocketObserver = ServerWebSocketObserver(InetSocketAddress(InetAddress.getLocalHost(), Server.port), observer)

    fun start() {
        loopbackServerWebSocketObserver.run()
        localhostServerWebSocketObserver.run()
    }

    fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        localhostServerWebSocketObserver.broadcast(message, clientSockets)
    }
}