package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import dev.robocode.tankroyale.gui.util.WsUrl
import java.net.URI
import java.util.*
import javax.crypto.KeyGenerator

object ServerSettings : PropertiesStore("Robocode Server Settings", "server.properties") {

    const val DEFAULT_SCHEME = "ws"
    const val DEFAULT_PORT = 7654
    const val LOCALHOST_URL = "$DEFAULT_SCHEME://localhost"

    private const val LOCAL_PORT = "local-port"
    private const val USE_REMOTE_SERVER = "use-remote-server"
    private const val USE_REMOTE_SERVER_URL = "use-remote-server-url"
    private const val REMOTE_SERVER_URLS = "remote-server-urls"
    private const val REMOTE_SERVER_BOT_SECRETS = "remote-server-bot-secrets"
    private const val REMOTE_SERVER_OBSERVER_SECRETS = "remote-server-observer-secrets"
    private const val CONTROLLER_SECRETS = "controller-secrets"
    private const val BOT_SECRETS = "bots-secrets"
    private const val INITIAL_POSITION_ENABLED = "initial-position-enabled"

    init {
        RegisterWsProtocol // work-around for ws:// with URI class
        load()

        onSaved.subscribe(this) { ServerEventTriggers.onRebootServer.fire(true /* setting changed */) }
    }

    fun serverUrl(): String = if (useRemoteServer) useRemoteServerUrl else localhostUrl()

    fun localhostUrl(): String = "$LOCALHOST_URL:$localPort"

    var localPort: Short
        get() {
            load()
            return properties.getProperty(LOCAL_PORT, "$DEFAULT_PORT").toShort()
        }
        set(value) {
            properties.setProperty(LOCAL_PORT, value.toString())
            save()
        }

    var useRemoteServer: Boolean
        get() {
            load()
            return properties.getProperty(USE_REMOTE_SERVER, "false").toBoolean()
        }
        set(value) {
            properties.setProperty(USE_REMOTE_SERVER, value.toString())
            save()
        }

    var useRemoteServerUrl: String
        get() {
            load()
            val url = properties.getProperty(USE_REMOTE_SERVER_URL, localhostUrl())
            return WsUrl(url).origin
        }
        set(value) {
            properties.setProperty(USE_REMOTE_SERVER_URL, value)
            save()
        }

    val serverPort: Int get() = URI(useRemoteServerUrl).port

    var remoteServerUrls: List<String>
        get() = loadIndexedProperties(REMOTE_SERVER_URLS)
        set(value) { saveIndexedProperties(REMOTE_SERVER_URLS, value) }

    var controllerSecrets: Set<String>
        get() = getPropertyAsSet(CONTROLLER_SECRETS).ifEmpty {
            controllerSecrets = setOf(generateSecret())
            controllerSecrets
        }
        set(value) {
            setPropertyBySet(CONTROLLER_SECRETS, value)
            save()
        }

    var botSecrets: Set<String>
        get() = getPropertyAsSet(BOT_SECRETS).ifEmpty {
            botSecrets = setOf(generateSecret())
            botSecrets
        }
        set(value) {
            setPropertyBySet(BOT_SECRETS, value)
            save()
        }

    var initialPositionsEnabled: Boolean
        get() {
            load()
            return properties.getProperty(INITIAL_POSITION_ENABLED, "false").toBoolean()
        }
        set(value) {
            properties.setProperty(INITIAL_POSITION_ENABLED, value.toString())
            save()
        }

    private fun loadIndexedProperties(propertyName: String): List<String> {
        load()

        val props = mutableListOf<String>()
        var index = 0
        while (true) {
            val value = properties["$propertyName.$index"] as String?
            if (value == null) break
            props.add(value)
            index++
        }
        return props
    }

    private fun saveIndexedProperties(propertyName: String, props: List<String>) {
        props.withIndex().forEach { (index, prop) ->
            properties["$REMOTE_SERVER_URLS.$index"] = prop
        }
        save()
    }

    private fun generateSecret(): String {
        val secretKey = KeyGenerator.getInstance("AES").generateKey()
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        // Remove trailing '=='
        return encodedKey.substring(0, encodedKey.length - 2)
    }
}