package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.server.Server
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import kotlin.system.exitProcess

class WebSocketObserver(observer: IClientWebSocketObserver) {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val webSocketServer: ServerWebSocketObserver =
        if (Server.useInheritedChannel) {
            validateInheritedChannel()
            ServerWebSocketObserver(observer)
        } else {
            ServerWebSocketObserver(InetSocketAddress(Server.portNumber), observer)
        }

    fun start() {
        webSocketServer.run()
    }

    fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        webSocketServer.broadcast(message, clientSockets)
    }

    private fun validateInheritedChannel() {
        val inheritedChannel = System.inheritedChannel()
        if (inheritedChannel == null) {
            log.error(
                "The '${Server.INHERIT}' mode require that a valid socket is passed to this server via file descriptor 3 (fd 3). " +
                        "Make sure the socket was passed correctly"
            )
            exitProcess(2)
        }
        if (inheritedChannel !is ServerSocketChannel) {
            log.error("The '${Server.INHERIT}' mode expects a server socket")
            exitProcess(2)
        }
    }
}