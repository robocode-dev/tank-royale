package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.game.*
import dev.robocode.tankroyale.server.connection.ClientWebSocketsHandler
import dev.robocode.tankroyale.server.core.ServerSetup
import org.java_websocket.WebSocket
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

    private val clientHandler = ClientWebSocketsHandler(setup, listener, controllerSecrets, botSecrets, ::broadcast)

    private val webSocketObserver = WebSocketObserver(clientHandler)

    fun start() {
        webSocketObserver.start()
    }

    fun stop() {
        clientHandler.close()
    }

    fun broadcastToObserverAndControllers(message: String) {
        broadcast(clientHandler.getObserverAndControllerSockets(), message)
    }

    fun mapToBotSockets(): Set<WebSocket> = clientHandler.getBotSockets()

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> = clientHandler.getBotHandshakes()

    fun mapToBotSockets(botAddresses: Collection<BotAddress>): Set<WebSocket> {
        val botSockets = mutableSetOf<WebSocket>()
        for (clientSocket in getBotHandshakes().keys) {
            addBotSocketIfMatching(clientSocket, botAddresses, botSockets)
        }
        return botSockets
    }

    private fun addBotSocketIfMatching(
        clientSocket: WebSocket,
        botAddresses: Collection<BotAddress>,
        botSockets: MutableSet<WebSocket>
    ) {
        clientSocket.remoteSocketAddress?.let { address ->
            botAddresses
                .firstOrNull { isAddressMatching(address, it) }
                ?.let { botSockets.add(clientSocket) }
        }
    }

    private fun isAddressMatching(address: InetSocketAddress, botAddress: BotAddress) =
        toIpAddress(address) == toIpAddress(botAddress) && botAddress.port == address.port

    private fun toIpAddress(address: InetSocketAddress) =
        address.address.hostAddress

    private fun toIpAddress(botAddress: BotAddress) =
        localhostToIpAddress(InetAddress.getByName(botAddress.host).hostAddress)

    private fun localhostToIpAddress(hostname: String) =
        if (hostname.equals("localhost", true)) "127.0.0.1" else hostname

    fun send(clientSocket: WebSocket, message: String) {
        log.debug("Send message: $message")
        clientHandler.send(clientSocket, message)
    }

    fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        log.debug("Broadcast message: $message")
        webSocketObserver.broadcast(clientSockets, message)
    }
}
