package dev.robocode.tankroyale.ui.desktop.settings


object ServerSettings : PropertiesStore("Robocode Server Config", "server.properties") {

    private const val DEFAULT_PORT: Int = 55000

    private const val DEFAULT_ENDPOINT_PROPERTY = "default.endpoint"
    private const val DEFAULT_ENDPOINT_VALUE = "ws://localhost:$DEFAULT_PORT"

    private const val START_LOCAL_SERVER_PROPERTY = "start_local_server"
    private const val START_LOCAL_SERVER_VALUE = true

    private const val USER_ENDPOINTS_PROPERTY = "user.endpoints"

    var endpoint: String
        get() {
            var endpoint = properties.getProperty(DEFAULT_ENDPOINT_PROPERTY, DEFAULT_ENDPOINT_VALUE)

            // Make sure the endpoint starts with "ws://"
            if (!endpoint.startsWith("ws://", ignoreCase = true)) {
                endpoint = "ws://$endpoint"
            }
            // Add a (default) port number, if it is not specified
            if (!endpoint.contains(Regex(".*:\\d{1,5}$"))) {
                endpoint = "$endpoint:$DEFAULT_PORT"
            }
            return endpoint
        }
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
