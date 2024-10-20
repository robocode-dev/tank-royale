package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.config.ServerConfigDialog
import dev.robocode.tankroyale.gui.util.MessageDialog
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object Server {

    private fun isRunning() = ServerProcess.isRunning() || RemoteServer.isRunning()

    fun connectOrStart(): Boolean /* connected and started */ {
        try {
            if (ServerSettings.useRemoteServer) {
                // use remote server

                if (RemoteServer.isRunning()) {
                    connectToServer()
                    return true // connected
                } else {
                    val message = String.format(
                        Messages.get("cannot_connect_to_remote_server"),
                        ServerSettings.useRemoteServerUrl
                    )
                    MessageDialog.showError(message)

                    ServerConfigDialog().isVisible = true

                    return false
                }
            } else {
                // use local server

                if (Client.isGameRunning()) {
                    if (!MessageDialog.showConfirm(Messages.get("stop_battle"))) {
                        return false // not started
                    }
                    Client.stopGame()
                }
                if (!isRunning()) {
                    startLocal()
                }
                connectToServer()
                return true // connected
            }
        } catch (e: Exception) {
            System.err.println(e.message)
            return false // not connected
        }
    }

    private fun connectToServer() {
        if (Client.isConnected()) {
            return
        }

        val connected = CountDownLatch(1)

        ClientEvents.onConnected.subscribe(this) {
            connected.countDown()
            ServerEvents.onConnected.fire(Unit)
        }
        // An exception can occur when trying to connect to the server.
        // Hence, we retry connecting, when it fails.
        var attempts = 5
        var exception: Exception? = null
        while (connected.count > 0 && attempts-- > 0) {
            try {
                Client.connect()
            } catch (ex: Exception) {
                exception = ex
            }
            connected.await(500, TimeUnit.MILLISECONDS)
        }
        if (attempts < 0 && exception != null) {
            throw exception
        }
    }

    fun startLocal() {
        val latch = CountDownLatch(1)
        ServerEvents.onStarted.subscribe(this) {
            latch.countDown()
        }
        ServerProcess.start()
        latch.await(1, TimeUnit.SECONDS) // wait till server has started
    }

    fun stopLocal() {
        Client.close()
        ServerProcess.stop()
    }

    fun rebootLocal() {
        stopLocal()
        startLocal()
    }
}