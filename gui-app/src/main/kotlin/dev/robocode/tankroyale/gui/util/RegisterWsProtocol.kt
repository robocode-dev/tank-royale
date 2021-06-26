package dev.robocode.tankroyale.gui.util

import java.io.IOException
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
        registerProtocol()
    }

    private fun registerProtocol() {
        URL.setURLStreamHandlerFactory { protocol ->
            if ("ws" == protocol || "wss" == protocol)
                object : URLStreamHandler() {
                    @Throws(IOException::class)
                    override fun openConnection(url: URL): URLConnection {
                        return object : URLConnection(url) {
                            @Throws(IOException::class)
                            override fun connect() {
                            }
                        }
                    }
                }
            else
                null
        }
    }
}