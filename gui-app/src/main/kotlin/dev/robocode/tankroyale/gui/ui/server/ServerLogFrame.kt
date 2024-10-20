package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.ui.console.ConsoleFrame
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onOpened

object ServerLogFrame : ConsoleFrame("local_server_log_frame") {

    init {
        setSize(700, 550)

        onOpened {
            setLocationRelativeTo(owner) // center on owner window
        }
    }
}
