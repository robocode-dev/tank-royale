package dev.robocode.tankroyale.gui.util

import java.awt.EventQueue

object GuiTask {

    fun enqueue(runnable: () -> Unit) {
        BusyCursor.activate()
        EventQueue.invokeLater {
            runnable.invoke()
            BusyCursor.deactivate()
        }
    }
}