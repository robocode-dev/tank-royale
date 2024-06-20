package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.connection.ClientWebSocketHandler
import dev.robocode.tankroyale.server.core.ServerSetup
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class ConnectionHandler(
    private val setup: ServerSetup,
    private val listener: IConnectionListener,
    private val controllerSecrets: Set<String>,
    private val botSecrets: Set<String>
) {
    private val log = LoggerFactory.getLogger(ConnectionHandler::class.java)

    private val address = InetSocketAddress(Server.port)
    private val webSocketObserver = WebSocketObserver(address).apply {
        isTcpNoDelay = true
    }

    private val clientHandler = ClientWebSocketHandler(webSocketObserver, setup, listener, controllerSecrets, botSecrets)

    fun start() {
        webSocketObserver.run()
    }

    fun stop() {
        clientHandler.stop()
    }

    fun broadcastToObserverAndControllers(message: String) {
        broadcast(getObserverAndControllerConnections(), message)
    }

    fun getBotConnections(): Set<WebSocket> = clientHandler.getBotConnections()

    fun getObserverAndControllerConnections(): Set<WebSocket> = clientHandler.getObserverAndControllerConnections()

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> = clientHandler.getBotHandshakes()

    fun getBotConnections(botAddresses: Collection<BotAddress>): Set<WebSocket> =
        mutableSetOf<WebSocket>().apply {
            getBotHandshakes().keys.forEach { clientSocket ->
                addToFoundConnection(clientSocket, botAddresses, this)
            }
        }

    private fun addToFoundConnection(
        clientSocket: WebSocket,
        botAddresses: Collection<BotAddress>,
        foundConnections: MutableSet<WebSocket>
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

    private fun shutdownAndAwaitTermination(pool: ExecutorService) {
        pool.apply {
            shutdown() // Disable new tasks from being submitted
            try {
                if (!awaitTermination(5, TimeUnit.SECONDS)) {
                    shutdownNow()
                    if (!awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("Pool did not terminate")
                    }
                }
            } catch (ex: InterruptedException) {
                shutdownNow()
                Thread.currentThread().interrupt()
            }
        }
    }

    fun send(clientSocket: WebSocket, message: String) {
        clientHandler.send(clientSocket, message)
    }

    fun broadcast(clients: Collection<WebSocket>, message: String) {
        log.debug("Broadcast message: $message")
        webSocketObserver.broadcast(message, clients)
    }

    private inner class WebSocketObserver(address: InetSocketAddress) : WebSocketServer(address) {

        override fun onStart() {
            // Do nothing
        }

        override fun onOpen(clientSocket: WebSocket, handshake: ClientHandshake) {
            clientHandler.onOpen(clientSocket)
        }

        override fun onClose(clientSocket: WebSocket, code: Int, reason: String, remote: Boolean) {
            clientHandler.onClose(clientSocket, code, reason, remote)
        }

        override fun onMessage(clientSocket: WebSocket, message: String) {
            clientHandler.onMessage(clientSocket, message)
        }

        override fun onError(clientSocket: WebSocket, ex: Exception) {
            clientHandler.onError(clientSocket, ex)
        }
    }
}
