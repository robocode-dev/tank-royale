package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.ResourceBundles
import dev.robocode.tankroyale.gui.util.Event
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane.*

object Server {

    val onConnected = Event<Unit>()

    init {
        ServerEventChannel.onStartServer.subscribe(Server) {
            startServerProcess()
        }
    }

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
            startServerProcess()
        }
        connectToServer()
    }

    private fun startServerProcess() {
        val latch = CountDownLatch(1)
        ServerProcess.apply {
            onStarted.subscribe(Server) { latch.countDown() }
            start()
        }
        latch.await(200, TimeUnit.MILLISECONDS) // wait till server has started
    }

    private fun connectToServer() {
        Client.apply {
            onConnected.subscribe(Server) { Server.onConnected.fire(Unit) }
            connect(WsUrl(ServerSettings.serverUrl).origin)
        }
    }

    private fun showStopGameDialog(): Int = showConfirmDialog(null,
        ResourceBundles.MESSAGES.get("stop_battle"),
        ResourceBundles.UI_TITLES.get("warning"),
        YES_NO_OPTION
    )
}