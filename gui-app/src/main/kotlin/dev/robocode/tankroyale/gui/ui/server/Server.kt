package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.util.MessageDialog.showConfirm
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object Server {

    fun isRunning() = ServerProcess.isRunning() || RemoteServer.isRunning()

    fun connectOrStart(): Boolean /* started */ {
        try {
            if (Client.isGameRunning()) {
                if (!showConfirm(Messages.get("stop_battle"))) {
                    return false // not started
                }
                Client.stopGame()
            }
            if (!isRunning()) {
                start()
            }
            connectToServer()
            return true // started

        } catch (e: Exception) {
            System.err.println(e.message)
            return false // not started
        }
    }

    private fun connectToServer() {
        val connected = CountDownLatch(1)

        ClientEvents.onConnected.subscribe(this) {
            connected.countDown()
            ServerEvents.onConnected.fire(Unit)
        }
        // An exception can occur when trying to connect to the server.
        // Hence, we retry connecting, when it fails.
        var attempts = 5
        while (connected.count > 0 && attempts-- > 0) {
            try {
                Client.connect()
            } catch (ignore: Exception) {
                // Do nothing, we try again within a loop
            }
            connected.await(500, TimeUnit.MILLISECONDS)
        }
    }

    fun start() {
        val latch = CountDownLatch(1)
        ServerEvents.onStarted.subscribe(this) {
            latch.countDown()
        }
        ServerProcess.start()
        latch.await(1, TimeUnit.SECONDS) // wait till server has started
    }

    fun stop() {
        Client.close()
        ServerProcess.stop()
    }

    fun reboot() {
        stop()
        connectToServer()
    }
}