package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.ICommand
import kotlinx.serialization.ImplicitReflectionSerializer
import javax.swing.JOptionPane

class PrepareServerCommand : ICommand {

    @ImplicitReflectionSerializer
    override fun execute() {
        if (ServerProcess.isRunning()) {
            val option = JOptionPane.showConfirmDialog(
                null,
                String.format(
                    ResourceBundles.MESSAGES.get("server_already_running"),
                    ServerProcess.port,
                    ServerProcess.gameType
                ),
                ResourceBundles.UI_TITLES.get("question"),
                JOptionPane.YES_NO_OPTION
            )
            if (option == JOptionPane.YES_OPTION) {
                ServerProcess.stop()
                StartServerDialog.isVisible = true
                return
            }
        }
        ConnectToServerCommand(ServerSettings.defaultUrl).execute()
    }
}