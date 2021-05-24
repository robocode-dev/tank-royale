package dev.robocode.tankroyale.gui.ui.new_server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.util.Event
import java.util.concurrent.CountDownLatch

object ConnectOrStartServerCommand : Runnable {

    val onConnected = Event<Unit>()

    override fun run() {
        if (Client.isGameRunning) {
            throw UnsupportedOperationException(
                "Game is still running. Show dialog to show if the user wants to abort the running game.")
        }
        if (isServerNotRunning()) {
            startServerProcess()
        }
        connectToServer()
    }

    private fun isServerNotRunning() = !ServerProcess.isRunning() && !RemoteServer.isRunning()

    private fun startServerProcess() {
        val latch = CountDownLatch(1)
        ServerProcess.apply {
            onStarted.subscribe(ConnectOrStartServerCommand) { latch.countDown() }
            start()
        }
        latch.await() // wait till server has started
    }

    private fun connectToServer() {
        Client.apply {
            onConnected.subscribe(ConnectOrStartServerCommand) {
                ConnectOrStartServerCommand.onConnected.fire(Unit)
            }
            connect(WsUrl(ServerSettings.serverUrl).origin)
        }
    }
}