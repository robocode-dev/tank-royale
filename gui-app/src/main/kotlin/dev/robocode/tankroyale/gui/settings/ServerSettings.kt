package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import dev.robocode.tankroyale.gui.util.WsUrl
import java.util.*
import javax.crypto.KeyGenerator

object ServerSettings : PropertiesStore("Robocode Server Settings", "server.properties") {

    const val DEFAULT_SCHEME = "ws"
    const val DEFAULT_PORT = 7654

    private const val LOCALHOST_URL = "$DEFAULT_SCHEME://localhost"

    private const val CONTROLLER_SECRETS = "controller-secrets"
    private const val BOT_SECRETS = "bots-secrets"
    private const val LOCAL_PORT = "local-port"
    private const val USE_REMOTE_SERVER = "use-remote-server"
    private const val USE_REMOTE_SERVER_URL = "use-remote-server-url"
    private const val REMOTE_SERVER_URLS = "remote-server-urls"
    private const val REMOTE_SERVER_CONTROLLER_SECRETS = "remote-server-controller-secrets"
    private const val REMOTE_SERVER_BOT_SECRETS = "remote-server-bot-secrets"
    private const val INITIAL_POSITION_ENABLED = "initial-position-enabled"

    init {
        RegisterWsProtocol // work-around for ws:// with URI class

        onSaved.subscribe(this) { ServerEventTriggers.onRebootLocalServer.fire(true /* setting changed */) }
    }

    fun serverUrl(): String = if (useRemoteServer) useRemoteServerUrl else localhostUrl()

    private fun localhostUrl(): String = "$LOCALHOST_URL:$localPort"

    var localPort: Short
        get() = load(LOCAL_PORT, "$DEFAULT_PORT").toShort()
        set(value) {
            save(LOCAL_PORT, value.toString())
        }

    var useRemoteServer: Boolean
        get() = load(USE_REMOTE_SERVER, "false").toBoolean()
        set(value) {
            if (load(USE_REMOTE_SERVER) == null && !value) return

            save(USE_REMOTE_SERVER, value.toString())
        }

    var useRemoteServerUrl: String
        get() {
            val url = load(USE_REMOTE_SERVER_URL, localhostUrl())
            return WsUrl(url).origin
        }
        set(value) {
            save(USE_REMOTE_SERVER_URL, value)
        }

    var remoteServerUrls: ArrayList<String>
        get() = loadIndexedProperties(REMOTE_SERVER_URLS)
        set(value) {
            saveIndexedProperties(REMOTE_SERVER_URLS, value)
        }

    fun controllerSecret(): String {
        load()
        return controllerSecrets.first()
    }

    fun botSecret(): String {
        load()
        return botSecrets.first()
    }

    var controllerSecrets: Set<String>
        get() = if (useRemoteServer) {
            setOf(remoteServerControllerSecret())
        } else {
            getPropertyAsSet(CONTROLLER_SECRETS).ifEmpty {
                controllerSecrets = setOf(generateSecret())
                controllerSecrets
            }
        }
        set(value) {
            setPropertyBySet(CONTROLLER_SECRETS, value)
            save()
        }

    var botSecrets: Set<String>
        get() =
            if (useRemoteServer) {
                setOf(remoteServerBotSecret())
            } else { // local server
                getPropertyAsSet(BOT_SECRETS).ifEmpty {
                    botSecrets = setOf(generateSecret())
                    botSecrets
                }
            }
        set(value) {
            setPropertyBySet(BOT_SECRETS, value)
            save()
        }

    private fun remoteServerControllerSecret(): String {
        val index = remoteServerUrls.indexOf(useRemoteServerUrl)
        return if (index >= 0) remoteServerControllerSecrets[index] else ""
    }

    private fun remoteServerBotSecret(): String {
        val index = remoteServerUrls.indexOf(useRemoteServerUrl)
        return if (index >= 0) remoteServerBotSecrets[index] else ""
    }

    var initialPositionsEnabled: Boolean
        get() = load(INITIAL_POSITION_ENABLED, "false").toBoolean()
        set(value) {
            save(INITIAL_POSITION_ENABLED, value.toString())
        }

    fun addRemoteServer(serverUrl: String, controllerSecret: String, botSecret: String) {
        val updatedRemoteServerUrls = ArrayList(remoteServerUrls)
        val updatedRemoteServerControllerSecrets = ArrayList(remoteServerControllerSecrets)
        val updatedRemoteServerBotSecrets = ArrayList(remoteServerBotSecrets)

        updatedRemoteServerUrls += serverUrl.trim()
        updatedRemoteServerControllerSecrets += controllerSecret.trim()
        updatedRemoteServerBotSecrets += botSecret.trim()

        saveIndexedProperties(REMOTE_SERVER_URLS, updatedRemoteServerUrls)
        saveIndexedProperties(REMOTE_SERVER_CONTROLLER_SECRETS, updatedRemoteServerControllerSecrets)
        saveIndexedProperties(REMOTE_SERVER_BOT_SECRETS, updatedRemoteServerBotSecrets)
    }

    fun removeRemoteServer(serverUrl: String) {
        val index = remoteServerUrls.indexOf(serverUrl.trim())
        if (index < 0) throw IllegalStateException("Remote server url not found: $serverUrl")

        val updatedRemoteServerUrls = ArrayList(remoteServerUrls)
        val updatedRemoteServerControllerSecrets = ArrayList(remoteServerControllerSecrets)
        val updatedRemoteServerBotSecrets = ArrayList(remoteServerBotSecrets)

        try {
            updatedRemoteServerUrls.removeAt(index)
            updatedRemoteServerControllerSecrets.removeAt(index)
            updatedRemoteServerBotSecrets.removeAt(index)
        } catch (_: IndexOutOfBoundsException) {
        }

        saveIndexedProperties(REMOTE_SERVER_URLS, updatedRemoteServerUrls)
        saveIndexedProperties(REMOTE_SERVER_CONTROLLER_SECRETS, updatedRemoteServerControllerSecrets)
        saveIndexedProperties(REMOTE_SERVER_BOT_SECRETS, updatedRemoteServerBotSecrets)
    }

    fun updateRemoteServer(oldServerUrl: String, newServerUrl: String, controllerSecret: String, botSecret: String) {
        removeRemoteServer(oldServerUrl)
        addRemoteServer(newServerUrl, controllerSecret, botSecret)
    }

    fun getRemoteServerData(serverUrl: String): RemoteServerData {
        val index = remoteServerUrls.indexOf(serverUrl.trim())
        if (index < 0) throw IllegalStateException("Remote server url '$serverUrl' not found")

        val controllerSecret = remoteServerControllerSecrets[index]
        val botSecret = remoteServerBotSecrets[index]
        return RemoteServerData(serverUrl, controllerSecret, botSecret)
    }

    private fun generateSecret(): String {
        val secretKey = KeyGenerator.getInstance("AES").generateKey()
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        // Remove trailing '=='
        return encodedKey.substring(0, encodedKey.length - 2)
    }

    private var remoteServerControllerSecrets: ArrayList<String>
        get() = loadIndexedProperties(REMOTE_SERVER_CONTROLLER_SECRETS)
        set(value) {
            saveIndexedProperties(REMOTE_SERVER_CONTROLLER_SECRETS, value)
        }

    private var remoteServerBotSecrets: ArrayList<String>
        get() = loadIndexedProperties(REMOTE_SERVER_BOT_SECRETS)
        set(value) {
            saveIndexedProperties(REMOTE_SERVER_BOT_SECRETS, value)
        }

    class RemoteServerData(
        val serverUrl: String,
        val controllerSecret: String,
        val botSecret: String
    )
}