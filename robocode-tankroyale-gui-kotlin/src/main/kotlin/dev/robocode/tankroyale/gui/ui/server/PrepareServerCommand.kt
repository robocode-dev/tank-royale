package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainWindowMenu
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.util.ICommand
import javax.swing.JOptionPane
import dev.robocode.tankroyale.gui.ui.ResourceBundles.STRINGS

object PrepareServerCommand : ICommand {

    init {
        MainWindowMenu.onStartServer.subscribe { execute() }
    }

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
                    ServerProcess.gameType.displayName
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