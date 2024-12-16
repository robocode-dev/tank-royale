package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

class ServerWebSocketObserver : WebSocketServer {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val observer: IClientWebSocketObserver
    private val useInheritedChannel: Boolean

    constructor(
        address: InetSocketAddress,
        observer: IClientWebSocketObserver
    ) : super(address) {
        this.observer = observer
        this.useInheritedChannel = false
        isTcpNoDelay = true
    }

    constructor(
        observer: IClientWebSocketObserver
    ) : super(
        (System.inheritedChannel() as? ServerSocketChannel)
            ?: throw IllegalStateException("No inherited server socket channel available")
    ) {
        this.observer = observer
        this.useInheritedChannel = true
        isTcpNoDelay = true
    }

    override fun onStart() {
        log.debug("onStart(){}", if (useInheritedChannel) " with inherited channel" else "")
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

    override fun onError(clientSocket: WebSocket?, exception: Exception) {
        observer.onError(clientSocket, exception)
    }
}
