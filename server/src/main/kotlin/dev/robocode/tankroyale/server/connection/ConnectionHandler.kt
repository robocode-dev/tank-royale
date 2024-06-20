package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.connection.ClientSocketsHandler
import dev.robocode.tankroyale.server.core.ServerSetup
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress

class ConnectionHandler(
    setup: ServerSetup,
    listener: IConnectionListener,
    controllerSecrets: Set<String>,
    botSecrets: Set<String>,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val address = InetSocketAddress(Server.port)
    private val serverSocketObserver = ServerSocketObserver(address).apply {
        isTcpNoDelay = true
    }

    private val clientHandler = ClientSocketsHandler(serverSocketObserver, setup, listener, controllerSecrets, botSecrets)

    fun start() {
        serverSocketObserver.run()
    }

    fun stop() {
        clientHandler.stop()
    }

    fun broadcastToObserverAndControllers(message: String) {
        broadcast(clientHandler.getObserverAndControllerSockets(), message)
    }

    fun mapToBotSockets(): Set<WebSocket> = clientHandler.getBotSockets()

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> = clientHandler.getBotHandshakes()

    fun mapToBotSockets(botAddresses: Collection<BotAddress>): Set<WebSocket> =
        mutableSetOf<WebSocket>().apply {
            getBotHandshakes().keys.forEach { clientSocket ->
                addToFoundSocket(clientSocket, botAddresses, this)
            }
        }

    private fun addToFoundSocket(
        clientSocket: WebSocket,
        botAddresses: Collection<BotAddress>,
        foundConnections: MutableSet<WebSocket>,
    ) {
        clientSocket.remoteSocketAddress?.let { address ->
            botAddresses.forEach { botAddress ->
                if (toIpAddress(address) == toIpAddress(botAddress) && botAddress.port == address.port) {
                    foundConnections += clientSocket
                    return@forEach
                }
            }
        }
    }

    private fun toIpAddress(address: InetSocketAddress) =
        localhostToIpAddress(address.hostName)

    private fun toIpAddress(botAddress: BotAddress) =
        localhostToIpAddress(InetAddress.getByName(botAddress.host).hostAddress)

    private fun localhostToIpAddress(hostname: String) =
        if (hostname.equals("localhost", true)) "127.0.0.1" else hostname

    fun send(clientSocket: WebSocket, message: String) {
        clientHandler.send(clientSocket, message)
    }

    fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        log.debug("Broadcast message: $message")
        serverSocketObserver.broadcast(message, clientSockets)
    }

    private inner class ServerSocketObserver(address: InetSocketAddress) : WebSocketServer(address) {

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
}
