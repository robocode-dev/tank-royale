package dev.robocode.tankroyale.ui.desktop.settings

import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings.endpoint
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings.userEndpoints
import dev.robocode.tankroyale.ui.desktop.util.WsEndpoint


object ServerSettings : PropertiesStore("Robocode Server Config", "server.properties") {

    const val DEFAULT_PORT: Int = 55000

    private const val DEFAULT_ENDPOINT_PROPERTY = "default.endpoint"
    private const val DEFAULT_ENDPOINT_VALUE = "ws://localhost:$DEFAULT_PORT"

    private const val USER_ENDPOINTS_PROPERTY = "user.endpoints"

    var endpoint: String
        get() {
            val endpoint = properties.getProperty(DEFAULT_ENDPOINT_PROPERTY, DEFAULT_ENDPOINT_VALUE)
            return WsEndpoint(endpoint).origin
        }
        set(value) {
            properties.setProperty(DEFAULT_ENDPOINT_PROPERTY, value)
        }

    var userEndpoints: List<String>
        get() {
            val endpoints = properties.getProperty(USER_ENDPOINTS_PROPERTY, "")
            return if (endpoints.isBlank()) {
                listOf(endpoint)
            } else {
                endpoints.split(";")
            }
        }
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
        userEndpoints = emptyList()
    }
}
