package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.server.Server
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.system.exitProcess

class MultiServerWebSocketObserver(observer: IClientWebSocketObserver) {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val webSocketServer = if (Server.useInheritedChannel) {
        if (System.inheritedChannel() == null) {
            log.error("'${Server.INHERIT}' (socket activation) is not supported on this system.")
            exitProcess(2)
        }
        arrayOf(ServerWebSocketObserver(observer))
    } else {
        arrayOf(
            ServerWebSocketObserver(InetSocketAddress(Server.portNumber), observer),
            ServerWebSocketObserver(InetSocketAddress(InetAddress.getLocalHost(), Server.portNumber), observer)
        )
    }

    fun start() {
        webSocketServer.forEach { it.run() }
    }

    fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        webSocketServer.forEach { it.broadcast(message, clientSockets) }
    }
}