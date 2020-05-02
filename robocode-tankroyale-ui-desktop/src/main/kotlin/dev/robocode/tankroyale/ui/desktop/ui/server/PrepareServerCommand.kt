package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.ICommand
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import javax.swing.JOptionPane
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles.STRINGS

@UnstableDefault
class PrepareServerCommand : ICommand {

    @ImplicitReflectionSerializer
    override fun execute() {
        if (ServerProcess.isRunning()) {
            val options = arrayOf(
                STRINGS.get("use"),
                STRINGS.get("restart")
            )
            val option = JOptionPane.showOptionDialog(
                null,
                String.format(
                    ResourceBundles.MESSAGES.get("server_already_running"),
                    ServerProcess.port,
                    ServerProcess.gameType
                ),
                ResourceBundles.UI_TITLES.get("question"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0] // default button
            )
            if (option == JOptionPane.NO_OPTION) {
                ServerProcess.stop()
                StartServerDialog.isVisible = true
                return
            }
        }
        ConnectToServerCommand(ServerSettings.defaultUrl).execute()
    }
}