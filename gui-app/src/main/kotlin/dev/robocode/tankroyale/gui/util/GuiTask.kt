package dev.robocode.tankroyale.gui.util

import java.awt.EventQueue

object GuiTask {

    fun enqueue(callable: () -> Unit) {
        BusyCursor.activate()
        EventQueue.invokeLater {
            callable.invoke()
            BusyCursor.deactivate()
        }
    }
}