package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.ui.MainWindow
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JOptionPane

object ServerActions {
    init {
        with(ServerEventTriggers) {
            onStartServer.subscribe(this) {
                Server.start()
            }
            onStopServer.subscribe(this) {
                Server.stop()
                BootProcess.stopRunningBots()
            }
            onRestartServer.subscribe(this) {
                handleRestart()
            }
        }
    }

    private val isRestarting = AtomicBoolean()

    private fun handleRestart() {
        if (!Server.isRunning() || isRestarting.get()) return

        isRestarting.set(true)

        val title = ResourceBundles.UI_TITLES.get("question")
        val question = ResourceBundles.STRINGS.get("restart_server_confirmation")

        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                MainWindow,
                question,
                title,
                JOptionPane.YES_NO_OPTION
            )
        ) {
            BootProcess.stopRunningBots()
            Server.restart()
        }
        isRestarting.set(false)
    }
}