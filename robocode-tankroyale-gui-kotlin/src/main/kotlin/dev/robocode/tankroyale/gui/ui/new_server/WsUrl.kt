package dev.robocode.tankroyale.gui.ui.new_server

import dev.robocode.tankroyale.gui.settings.ServerSettings
import java.net.URI

/**
 * Convenient class for getting the origin url from a partial url like e.g. `localhost`, `ws://localhost`, and
 * `localhost:80`, which are all interpreted as `ws://localhost:80`.
 * If the scheme is missing,[ServerSettings.DEFAULT_SCHEME] is being used.
 * If the port is missing,[ServerSettings.DEFAULT_PORT] is being used.
 */
class WsUrl(partialUrl: String) {

    val uri: URI

    init {
        var uri = URI(partialUrl)
        if (!partialUrl.contains("://")) {
            uri = URI("${ServerSettings.DEFAULT_SCHEME}://$partialUrl")
        }
        if (uri.port == -1) {
            uri = URI("${uri.scheme}://${uri.host}:${ServerSettings.DEFAULT_PORT}")
        }
        this.uri = uri
    }

    // "origin" is a combination of a scheme/protocol, hostname, and port
    val origin: String get() = uri.toURL().toString()

    override fun equals(other: Any?): Boolean = (other is WsUrl && uri == other.uri)

    companion object {
        fun isValidWsUrl(url: String): Boolean {
            val str = url.trim()
            return str.isNotBlank() &&
                    str.matches(Regex("^(ws(s)://)?(\\p{L})?(\\p{L}|\\.|[-])*(\\p{L})(:\\d{1,5})?$"))
        }
    }
}

fun main() {
    val partialUrl = "localhost"
    println(WsUrl(partialUrl).uri.scheme)
    println(WsUrl(partialUrl).uri.host)
    println(WsUrl(partialUrl).uri.port)
}