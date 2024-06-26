package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.server.connection.ClientSocketsHandler
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class ServerSocketObserver(
    address: InetSocketAddress,
    private val clientHandler: ClientSocketsHandler
) : WebSocketServer(address) {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun onStart() {
        log.debug("onStart()")
    }

    override fun onOpen(clientSocket: WebSocket, handshake: ClientHandshake) {
        log.debug("onOpen(): client: {}", clientSocket.remoteSocketAddress)
        clientHandler.addSocketAndSendServerHandshake(clientSocket)
    }

    override fun onClose(clientSocket: WebSocket, code: Int, reason: String, remote: Boolean) {
        log.debug("onClose: client:{}, code: {}, reason: {}, remote: {}", clientSocket.remoteSocketAddress, code, reason, remote)
        clientHandler.removeSocket(clientSocket)
    }

    override fun onMessage(clientSocket: WebSocket, message: String) {
        log.debug("onMessage: client: {}, message: {}", clientSocket.remoteSocketAddress, message)
        clientHandler.processMessage(clientSocket, message)
    }

    override fun onError(clientSocket: WebSocket, ex: Exception) {
        log.error("onError: client: ${clientSocket.remoteSocketAddress}, message: ${ex.message}")
    }
}
