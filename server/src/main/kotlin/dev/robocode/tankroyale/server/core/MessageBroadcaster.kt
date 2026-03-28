package dev.robocode.tankroyale.server.core

import com.google.gson.Gson
import dev.robocode.tankroyale.schema.BotInfo
import dev.robocode.tankroyale.schema.BotListUpdate
import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.mapper.BotHandshakeToBotInfoMapper
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException

/** Broadcaster for sending messages to bots and observers. */
class MessageBroadcaster(
    private val connectionHandler: ConnectionHandler,
    private val gson: Gson
) {
    @Volatile
    private var botListUpdateMessage = BotListUpdate().apply {
        type = Message.Type.BOT_LIST_UPDATE
        bots = emptyList()
    }

    fun send(conn: WebSocket, msg: Message) {
        requireNotNull(msg.type) { "'type' is required on the message" }
        gson.toJson(msg).also {
            try {
                conn.send(it)
            } catch (_: WebsocketNotConnectedException) {
                // Bot cannot receive events and send new intents.
            }
        }
    }

    fun broadcastToObserverAndControllers(msg: Message) {
        requireNotNull(msg.type) { "'type' is required on the message" }
        connectionHandler.broadcastToObserverAndControllers(gson.toJson(msg))
    }

    fun broadcastToAll(msg: Message, participants: Collection<WebSocket>) {
        requireNotNull(msg.type) { "'type' is required on the message" }
        val json = gson.toJson(msg)
        connectionHandler.broadcastToObserverAndControllers(json)
        connectionHandler.broadcast(participants, json)
    }

    fun updateBotListUpdateMessage() {
        val newBotsList = mutableListOf<BotInfo>()

        connectionHandler.apply {
            mapToBotSockets().forEach { conn ->
                getBotHandshakes()[conn]?.let { botHandshake ->
                    conn.remoteSocketAddress.apply {
                        newBotsList.add(BotHandshakeToBotInfoMapper.map(botHandshake, hostString, port))
                    }
                }
            }
        }

        botListUpdateMessage = BotListUpdate().apply {
            type = Message.Type.BOT_LIST_UPDATE
            bots = newBotsList
        }
    }

    fun broadcastBotListUpdate() {
        broadcastToObserverAndControllers(botListUpdateMessage)
    }

    fun sendBotListUpdate(conn: WebSocket) {
        send(conn, botListUpdateMessage)
    }
}
