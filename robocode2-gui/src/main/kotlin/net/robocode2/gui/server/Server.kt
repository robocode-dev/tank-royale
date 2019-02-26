package net.robocode2.gui.server

import com.beust.klaxon.Klaxon
import net.robocode2.gui.model.ServerHandshake
import net.robocode2.gui.utils.Observable
import java.net.URI

object Server {

    val onConnected = Observable<Unit>()
    val onDisconnected = Observable<Unit>()

    val onServerHandshake = Observable<ServerHandshake>()

    val defaultUri = URI("ws://localhost:50000")

    private var client: WebSocketClient = WebSocketClient(defaultUri)

    fun connect(uri: URI) {
        client = WebSocketClient(uri)

        client.onOpen.subscribe { onConnected.notify(Unit) }
        client.onClose.subscribe { onDisconnected.notify(Unit) }

        client.open() // must be called after onOpen.subscribe()

        client.onMessage.subscribe { onMessage(it) }
    }

    fun disconnect() {
        client.close()
    }

    fun isConnected() = client.isOpen()

    private fun onMessage(msg: String) {
        val message = Klaxon().parse<net.robocode2.gui.model.Message>(msg)
        when (message) {
            is ServerHandshake -> onServerHandshake.notify(message)
        }
    }
}