package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import java.awt.EventQueue.invokeLater
import javax.swing.JOptionPane

object ServerActions {
    init {
        ServerEventTriggers.apply {
            onStartServer.subscribe(this) {
                Server.start()
            }
            onStopServer.subscribe(this) {
                Server.stop()
                BootProcess.stopRunning()
            }
            onRestartServer.subscribe(this) {
                Server.restart()
            }
            onRebootServer.subscribe(this) {
                handleReboot()
            }
        }

        ServerEvents.onStarted.subscribe(this) {
            ServerLogWindow.clear()
        }
    }

    private fun handleReboot() {
        if (!ServerProcess.isRunning()) return

        val title = ResourceBundles.UI_TITLES.get("question")
        val question = ResourceBundles.STRINGS.get("restart_server_confirmation")

        invokeLater {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    MainWindow,
                    question,
                    title,
                    JOptionPane.YES_NO_OPTION
                )
            ) {
                BootProcess.stopRunning()
                Server.restart()
            }
        }
    }
}