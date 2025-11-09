package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.util.EDT.enqueue
import dev.robocode.tankroyale.gui.util.MessageDialog
import dev.robocode.tankroyale.gui.util.isRemoteEndpoint

object ServerActions {
    init {
        ServerEventTriggers.apply {
            onStartLocalServer.subscribe(this) {
                Server.startLocal()
            }
            onStopLocalServer.subscribe(this) {
                Server.stopLocal()
                BootProcess.stop()
            }
            onRebootLocalServer.subscribe(this) {
                handleRebootLocal(it)
            }
        }

        ServerEvents.onStarted.subscribe(this) {
            ServerLogFrame.clear()
        }
    }

    private fun handleRebootLocal(dueToSetting: Boolean) {
        if (!ServerProcess.isRunning() || isRemoteEndpoint(ServerSettings.serverUrl())) return

        val resourceKey =
            if (dueToSetting)
                "reboot_server_confirmation_settings"
            else
                "reboot_server_confirmation"

        if (MessageDialog.showConfirm(Messages.get(resourceKey))) {
            enqueue {
                BootProcess.stop()
                Server.rebootLocal()
            }
        }
    }
}