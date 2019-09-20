package dev.robocode.tankroyale.ui.desktop.settings

import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings.endpoint
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings.startLocalServer
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings.userEndpoints


object ServerSettings : PropertiesStore("Robocode Server Config", "server.properties") {

    private const val DEFAULT_ENDPOINT_PROPERTY = "default.endpoint"
    private const val DEFAULT_ENDPOINT_VALUE = "ws://localhost:55000"

    private const val START_LOCAL_SERVER_PROPERTY = "start_local_server"
    private const val START_LOCAL_SERVER_VALUE = true

    private const val USER_ENDPOINTS_PROPERTY = "user.endpoints"

    var endpoint: String
        get() = properties.getProperty(DEFAULT_ENDPOINT_PROPERTY, DEFAULT_ENDPOINT_VALUE)
        set(value) {
            properties.setProperty(DEFAULT_ENDPOINT_PROPERTY, value)
        }

    var startLocalServer: Boolean
        get() = properties.getProperty(START_LOCAL_SERVER_PROPERTY, "$START_LOCAL_SERVER_VALUE")!!.toBoolean()
        set(value) {
            properties.setProperty(START_LOCAL_SERVER_PROPERTY, "$value")
        }

    var userEndpoints: List<String>
        get() = properties.getProperty(USER_ENDPOINTS_PROPERTY, "").split(";")
        set(value) {
            val list = ArrayList(value)
            list.remove(DEFAULT_ENDPOINT_VALUE)
            properties.setProperty(USER_ENDPOINTS_PROPERTY, list.joinToString(separator = ";"))
        }

    val port: Int
        get() = endpoint.substring(endpoint.lastIndexOf(':') + 1).toInt()

    init {
        resetToDefault()
        load()
    }

    fun resetToDefault() {
        endpoint = DEFAULT_ENDPOINT_VALUE
        startLocalServer = true
        userEndpoints = emptyList()
    }
}