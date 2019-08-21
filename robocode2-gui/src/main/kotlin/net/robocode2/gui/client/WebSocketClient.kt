package net.robocode2.gui.client

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import net.robocode2.gui.model.Message
import net.robocode2.gui.model.messageModule
import net.robocode2.gui.utils.Event
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(private val uri: URI) : AutoCloseable {

    val onOpen = Event<Unit>()
    val onClose = Event<Unit>()
    val onMessage = Event<String>()
    val onError = Event<Exception>()

    private var client = Client()

    private val json = Json(context = messageModule)

    fun open() {
        client.connect()
    }

    override fun close() {
        client.close()
    }

    fun isOpen() = client.isOpen

    fun send(data: Any) {
        val msg = json.stringify(PolymorphicSerializer(Message::class), data)
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