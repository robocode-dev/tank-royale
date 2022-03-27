package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.util.WsUrl
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane.*

object Server {

    fun isRunning() = ServerProcess.isRunning() || RemoteServer.isRunning()

    fun connectOrStart() {
        if (Client.isGameRunning) {
            if (showStopGameDialog() == NO_OPTION) {
                return
            } else {
                Client.stopGame()
            }
        }
        if (!isRunning()) {
            start()
        }
        connectToServer()
    }

    private fun connectToServer() {
        var connected = false
        ClientEvents.onConnected.subscribe(Server) {
            ServerEvents.onConnected.fire(Unit)
            connected = true
        }
        // An exception can occur when trying to connect to the server.
        // Hence, we retry connecting, when it fails.
        var attempts = 5
        while (!connected && attempts-- > 0) {
            try {
                Client.connect(WsUrl(ServerSettings.serverUrl).origin)
            } catch (ignore: Exception) {
            }
            sleep(500)
        }
    }

    fun start() {
        val latch = CountDownLatch(1)
        ServerEvents.onStarted.subscribe(Server) {
            latch.countDown()
        }
        ServerProcess.start()
        latch.await(1000, TimeUnit.MILLISECONDS) // wait till server has started
    }

    fun stop() {
        ServerProcess.stop()
    }

    fun restart() {
        ServerProcess.restart()
    }

    private fun showStopGameDialog(): Int = showConfirmDialog(
        null,
        ResourceBundles.MESSAGES.get("stop_battle"),
        ResourceBundles.UI_TITLES.get("warning"),
        YES_NO_OPTION
    )
}