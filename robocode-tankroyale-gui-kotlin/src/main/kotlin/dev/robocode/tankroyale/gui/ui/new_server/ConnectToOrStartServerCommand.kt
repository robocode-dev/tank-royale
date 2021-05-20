package dev.robocode.tankroyale.gui.ui.new_server

import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.util.Event
import java.io.Closeable

object ConnectToOrStartServerCommand : Runnable {

    val onConnected = Event<Unit>()

    override fun run() {
        if (!ServerProcess.isRunning()) {
            if (!CheckWebSocketConnection.isRunning()) {
                var disposable: Closeable? = null
                disposable = ServerProcess.onStarted.subscribe {
                    disposable?.close()
                    onConnected.publish(Unit)
                }
                ServerProcess.start()
                return
            }
        }
        onConnected.publish(Unit)
    }
}