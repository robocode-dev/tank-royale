package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.util.ICommand
import dev.robocode.tankroyale.gui.util.WsUrl

class StartServerCommand(
    private val port: Int = ServerSettings.DEFAULT_PORT,
    private val gameType: GameType = GameType.CLASSIC
) : ICommand {

    override fun execute() {
        ServerProcess.start(gameType, port)
        val url = WsUrl("localhost:${port}").origin
        TestServerConnectionCommand(url).execute()
    }
}