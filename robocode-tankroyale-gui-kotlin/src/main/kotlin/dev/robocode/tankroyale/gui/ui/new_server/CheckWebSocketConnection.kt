package dev.robocode.tankroyale.gui.ui.new_server

import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.net.http.WebSocket.Listener
import java.time.Duration

object CheckWebSocketConnection {

    private val DEFAULT_CONNECTION_TIMEOUT = Duration.ofMillis(200)

    fun isRunning(uri: String, timeout: Duration? = DEFAULT_CONNECTION_TIMEOUT): Boolean = isRunning(URI(uri), timeout)

    fun isRunning(uri: URI, timeout: Duration? = DEFAULT_CONNECTION_TIMEOUT): Boolean {
        return try {
            val httpClient = HttpClient.newBuilder().build()
            val webSocketBuilder = httpClient.newWebSocketBuilder().connectTimeout(timeout)
            webSocketBuilder.buildAsync(uri, object : Listener {
                override fun onOpen(webSocket: WebSocket) {
                    webSocket.abort() // close connection
                }
            }).join()
            true
        } catch (ex: Exception) {
            false
        }
    }
}

fun main() {
    val isRunning = CheckWebSocketConnection.isRunning(URI("ws://localhost:80"))
    println("isRunning: $isRunning")
}