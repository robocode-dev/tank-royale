package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.server.ServerProcess

object ServerActions {
    init {
        with(ServerEvents) {
            onStartServer.subscribe(this) {
                ServerProcess.start()
            }
            onStopServer.subscribe(this) {
                ServerProcess.stop()
            }
            onRestartServer.subscribe(this) {
                ServerProcess.restart()
            }
        }
    }
}