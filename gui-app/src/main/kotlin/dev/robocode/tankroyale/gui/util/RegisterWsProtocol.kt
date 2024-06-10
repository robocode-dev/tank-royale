package dev.robocode.tankroyale.gui.util

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

/**
 * Registers the ws protocol in order to use ws://host:port with URI and URL classes.
 *
 * Link: https://stackoverflow.com/questions/26363573/registering-and-using-a-custom-java-net-url-protocol
 */
object RegisterWsProtocol {

    init {
        registerWebSocketProtocol()
    }

    private fun registerWebSocketProtocol() {
        URL.setURLStreamHandlerFactory { protocol ->
            when (protocol) {
                "ws", "wss" ->
                    object : URLStreamHandler() {
                        override fun openConnection(url: URL) = object : URLConnection(url) {
                            override fun connect() {
                                // Do nothing
                            }
                        }
                    }

                else -> null
            }
        }
    }
}