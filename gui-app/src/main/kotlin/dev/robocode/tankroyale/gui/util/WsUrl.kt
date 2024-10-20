package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.settings.ServerSettings
import java.net.URI

/**
 * Convenient class for getting the origin URL from a partial URL like e.g. `localhost`, `ws://localhost`, and
 * `localhost:80`, which are all interpreted as `ws://localhost:80`.
 * If the scheme is missing,[ServerSettings.DEFAULT_SCHEME] is being used.
 * If the port is missing,[ServerSettings.DEFAULT_PORT] is being used.
 */
class WsUrl(partialUrl: String) {

    val uri: URI

    init {
        // protocol + host + port, e.g. ws://localhost:80

        var origin = partialUrl

        // Make sure the URL starts with "ws://"
        if (!origin.startsWith("ws://")) {
            origin = "ws://$origin"
        }
        // Add a (default) port number, if it is not specified
        if (!origin.contains(Regex(".*:\\d{1,5}$"))) {
            origin = "$origin:${ServerSettings.DEFAULT_PORT}"
        }
        uri = URI(origin)
    }

    // "origin" is a combination of a scheme/protocol, hostname, and port
    val origin: String get() = uri.toURL().toString()

    override fun equals(other: Any?): Boolean = (other is WsUrl && uri == other.uri)

    override fun hashCode(): Int = uri.hashCode()

    companion object {
        fun isValidWsUrl(url: String): Boolean {
            val trimmedUrl = url.trim()
            if (trimmedUrl.isEmpty() || !trimmedUrl.startsWith("ws://") || trimmedUrl.endsWith(".")) {
                return false
            }
            return try {
                // https://docs.oracle.com/javase/8/docs/api/java/net/URI.html#parseServerAuthority--
                URI(trimmedUrl).parseServerAuthority()
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}