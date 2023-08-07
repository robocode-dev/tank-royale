package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.UiTitles
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane.*

object Server {

    fun isRunning() = ServerProcess.isRunning() || RemoteServer.isRunning()

    fun connectOrStart() {
        try {
            if (Client.isGameRunning()) {
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

        } catch (e: Exception) {
            System.err.println(e.message)
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

    private fun showStopGameDialog(): Int = showConfirmDialog(
        null,
        Messages.get("stop_battle"),
        UiTitles.get("warning"),
        YES_NO_OPTION
    )
}