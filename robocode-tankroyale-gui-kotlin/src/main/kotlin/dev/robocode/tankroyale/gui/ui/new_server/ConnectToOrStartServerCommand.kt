package dev.robocode.tankroyale.gui.ui.new_server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.util.Event

object ConnectToOrStartServerCommand : Runnable {

    val onConnected = Event<Unit>()

    override fun run() {
        // Connect to server, if one is running
        if (!ServerProcess.isRunning() && !RemoteServer.isRunning()) {
            ServerProcess.start()
        }

        Client.apply {
            onConnected.subscribe(this) { ConnectToOrStartServerCommand.onConnected.fire(Unit) }
            connect(WsUrl(ServerSettings.serverUrl).origin)
        }
    }
}