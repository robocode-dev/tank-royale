package net.robocode2.gui.settings

object ServerSettings : PropertiesStore("Robocode Server Config", "server.properties") {

    private const val SERVER_ENDPOINT_PROPERTY = "server.endpoint"
    const val DEFAULT_SERVER_ENDPOINT = "ws://localhost:55000"

    private const val REMOTE_SERVER_PROPERTY = "remote.server"
    private const val DEFAULT_REMOTE_SERVER = false

    var endpoint: String
        get() = properties.getProperty(SERVER_ENDPOINT_PROPERTY, DEFAULT_SERVER_ENDPOINT)
        set(endpoint) {
            properties.setProperty(SERVER_ENDPOINT_PROPERTY, endpoint)
        }

    var useRemoteServer: Boolean
        get() = properties.getProperty(REMOTE_SERVER_PROPERTY, "$DEFAULT_REMOTE_SERVER")!!.toBoolean()
        set(useRemoteServer) {
            properties.setProperty(REMOTE_SERVER_PROPERTY, "$useRemoteServer")
        }

    init {
        resetToDefault()
        load()
    }

    fun resetToDefault() {
        endpoint = DEFAULT_SERVER_ENDPOINT
        useRemoteServer = DEFAULT_REMOTE_SERVER
    }
}