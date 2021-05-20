package dev.robocode.tankroyale.gui.ui.new_server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ConnectToServerCommand
import dev.robocode.tankroyale.gui.util.Event
import java.io.Closeable

object ConnectToOrStartServerCommand : Runnable {

    val onConnected = Event<Unit>()

    override fun run() {
        // Connect to server, if one is running
        if (!ServerProcess.isRunning() && !RemoteServer.isRunning()) {
            ServerProcess.start()
        }

        Client.onConnected.subscribe { onConnected.publish(Unit) }
        Client.connect(WsUrl(ServerSettings.serverUrl).origin)
    }
}