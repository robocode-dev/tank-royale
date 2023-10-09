package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.UiTitles
import dev.robocode.tankroyale.gui.util.EDT.enqueue
import javax.swing.JOptionPane

object ServerActions {
    init {
        ServerEventTriggers.apply {
            onStartServer.subscribe(this) {
                Server.start()
            }
            onStopServer.subscribe(this) {
                Server.stop()
                BootProcess.stop()
            }
            onRebootServer.subscribe(this) {
                handleReboot(it)
            }
        }

        ServerEvents.onStarted.subscribe(this) {
            ServerLogFrame.clear()
        }
    }

    private fun handleReboot(dueToSetting: Boolean) {
        if (!ServerProcess.isRunning()) return

        val title = UiTitles.get("question")
        val resource =
            if (dueToSetting)
                "reboot_server_confirmation_settings"
            else
                "reboot_server_confirmation"

        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                MainFrame,
                Messages.get(resource),
                title,
                JOptionPane.YES_NO_OPTION
            )
        ) {
            enqueue {
                BootProcess.stop()
                Server.reboot()
            }
        }
    }
}