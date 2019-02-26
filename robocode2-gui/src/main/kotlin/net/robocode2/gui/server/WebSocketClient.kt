package net.robocode2.gui.server

import net.robocode2.gui.utils.Observable
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(private val uri: URI) {

    val onOpen = Observable<Unit>()
    val onClose = Observable<Unit>()
    val onMessage = Observable<String>()
    val onError = Observable<Exception>()

    private var client = Client()

    fun open() {
        client.connect()
    }

    fun close() {
        client.close()
    }

    fun isOpen() = client.isOpen

    private inner class Client : WebSocketClient(uri) {

        override fun onOpen(serverHandshake: ServerHandshake?) {
            onOpen.notify(Unit)
            println("onOpen: ")
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            onClose.notify(Unit)
            println("onClose: (code: $code, reason: $reason, remote: $remote)")
        }

        override fun onMessage(message: String) {
            onMessage.notify(message)
            println("onMessage: $message")
        }

        override fun onError(ex: Exception) {
            onError.notify(ex)
            println("onError: $ex")
        }
    }
}