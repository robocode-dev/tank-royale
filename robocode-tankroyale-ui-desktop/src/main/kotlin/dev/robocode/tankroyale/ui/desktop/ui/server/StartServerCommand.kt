package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.GameType
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.util.ICommand
import dev.robocode.tankroyale.ui.desktop.util.WsUrl
import kotlinx.serialization.ImplicitReflectionSerializer

class StartServerCommand(
    val port: Int = ServerSettings.DEFAULT_PORT,
    val gameType: GameType = GameType.CLASSIC
) : ICommand {

    @ImplicitReflectionSerializer
    override fun execute() {
        ServerProcess.start(port)
        val url = WsUrl("localhost:${port}").origin
        TestServerConnectionCommand(url).execute()
    }
}