package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.gui.util.WsUrl


object ServerSettings : PropertiesStore("Robocode Server Config", "server.properties") {

    const val DEFAULT_PORT: Int = 80
    const val DEFAULT_LOCALHOST_URL = "ws://localhost"

    private const val DEFAULT_URL_PROPERTY = "default.url"
    private const val USER_URLS_PROPERTY = "user.urls"

    var defaultUrl: String
        get() {
            val url = properties.getProperty(DEFAULT_URL_PROPERTY, DEFAULT_LOCALHOST_URL)
            return WsUrl(url).origin
        }
        set(value) {
            properties.setProperty(DEFAULT_URL_PROPERTY, value)
        }

    var userUrls: List<String>
        get() {
            val urls = properties.getProperty(USER_URLS_PROPERTY, "")
            return if (urls.isBlank()) {
                listOf(defaultUrl)
            } else {
                urls.split(",")
            }
        }
        set(value) {
            val list = ArrayList(value)
            list.remove(DEFAULT_LOCALHOST_URL)
            properties.setProperty(USER_URLS_PROPERTY, list.joinToString(separator = ","))
        }

    val port: Int
        get() = defaultUrl.substring(defaultUrl.lastIndexOf(':') + 1).toInt()

    init {
        resetToDefault()
        load()
    }

    private fun resetToDefault() {
        defaultUrl = DEFAULT_LOCALHOST_URL
        userUrls = emptyList()
    }
}
