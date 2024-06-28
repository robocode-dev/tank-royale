package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class ServerWebSocketObserver(
    address: InetSocketAddress,
    private val observer: IClientWebSocketObserver
) : WebSocketServer(address) {

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        // Disable Nagle's algorithm
        isTcpNoDelay = true
    }

    override fun onStart() {
        log.debug("onStart()")
    }

    override fun onOpen(clientSocket: WebSocket, handshake: ClientHandshake) {
        log.debug("onOpen(): client: {}", clientSocket.remoteSocketAddress)
        observer.onOpen(clientSocket, handshake)
    }

    override fun onClose(clientSocket: WebSocket, code: Int, reason: String, remote: Boolean) {
        log.debug("onClose: client:{}, code: {}, reason: {}, remote: {}", clientSocket.remoteSocketAddress, code, reason, remote)
        observer.onClose(clientSocket, code, reason, remote)
    }

    override fun onMessage(clientSocket: WebSocket, message: String) {
        log.debug("onMessage: client: {}, message: {}", clientSocket.remoteSocketAddress, message)
        observer.onMessage(clientSocket, message)
    }

    override fun onError(clientSocket: WebSocket, exception: Exception) {
        log.error("onError: client: ${clientSocket.remoteSocketAddress}, message: ${exception.message}")
        onError(clientSocket, exception)
    }
}
