package dev.robocode.tankroyale.ui.desktop.client

import dev.robocode.tankroyale.ui.desktop.model.Message
import dev.robocode.tankroyale.ui.desktop.model.MessageConstants
import dev.robocode.tankroyale.ui.desktop.util.Event
import kotlinx.serialization.PolymorphicSerializer
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(private val uri: URI) : AutoCloseable {

    val onOpen = Event<Unit>()
    val onClose = Event<Unit>()
    val onMessage = Event<String>()
    val onError = Event<Exception>()

    private var client = Client()

    private val json = MessageConstants.json

    fun open() {
        client.connect()
    }

    override fun close() {
        client.close()
    }

    fun isOpen() = client.isOpen

    fun send(data: Any) {
        val msg = json.encodeToString(PolymorphicSerializer(Message::class), data as Message)
        client.send(msg)
    }

    private inner class Client : WebSocketClient(uri) {

        override fun onOpen(serverHandshake: ServerHandshake?) {
            onOpen.publish(Unit)
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            onClose.publish(Unit)
        }

        override fun onMessage(message: String) {
            onMessage.publish(message)
        }

        override fun onError(ex: Exception) {
            onError.publish(ex)
        }
    }
}