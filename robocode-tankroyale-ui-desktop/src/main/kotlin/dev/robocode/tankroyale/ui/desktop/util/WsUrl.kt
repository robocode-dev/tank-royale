package dev.robocode.tankroyale.ui.desktop.util

import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import java.net.URI


class WsUrl(private val partialUrl: String) {

    private val uri: URI

    init {
        // protocol + host + port, e.g. ws://localhost:80

        var origin = partialUrl

        // Make sure the url starts with "ws://"
        if (!origin.startsWith("ws://", ignoreCase = true)) {
            origin = "ws://$origin"
        }
        // Add a (default) port number, if it is not specified
        if (!origin.contains(Regex(".*:\\d{1,5}$"))) {
            origin = "$origin:${ServerSettings.DEFAULT_PORT}"
        }
        uri = URI(origin)
    }

    val origin: String get() = uri.toURL().toString()

    val protocol: Int get() = uri.port

    val host: String get() = uri.host

    val port: Int get() = uri.port

    val isLocalhost: Boolean get() = InetAddressUtil.isLocalAddress(host)
}