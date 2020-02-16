package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.GameType
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.util.ICommand
import dev.robocode.tankroyale.ui.desktop.util.WsUrl
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault

@UnstableDefault
class StartServerCommand(
    private val port: Int = ServerSettings.DEFAULT_PORT,
    private val gameType: String = GameType.CLASSIC.type
) : ICommand {

    @ImplicitReflectionSerializer
    override fun execute() {
        ServerProcess.start(gameType, port)
        val url = WsUrl("localhost:${port}").origin
        TestServerConnectionCommand(url).execute()
    }
}