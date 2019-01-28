package net.robocode2.gui.server

import net.robocode2.gui.utils.Observable
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

object WebSocketClient {

    val onOpen = Observable<Unit>()
    val onClose = Observable<Unit>()
    val onMessage = Observable<String>()
    val onError = Observable<Exception>()

    private val serverUri: URI = URI("ws://localhost:50000")

    private var client = Client()

    fun open() {
        if (client.isClosed) {
            client = Client()
        }
        client.connect()
    }

    fun close() {
        client.close()
    }

    fun isOpen() = client.isOpen

    private class Client : WebSocketClient(serverUri) {

        override fun onOpen(serverHandshake: ServerHandshake?) {
            onOpen.notifyChange(Unit)
            println("onOpen")
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            onClose.notifyChange(Unit)
            println("onClose")
        }

        override fun onMessage(message: String) {
            onMessage.notifyChange(message)
            println("onMessage")
        }

        override fun onError(ex: Exception) {
            onError.notifyChange(ex)
            println("onError")
        }
    }
}