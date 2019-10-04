package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.util.ICommand

class StartServerCommand() : ICommand {

    override fun execute() {
        ServerProcess.start()
        Client.connect(ServerSettings.DEFAULT_LOCALHOST_URL)
    }
}