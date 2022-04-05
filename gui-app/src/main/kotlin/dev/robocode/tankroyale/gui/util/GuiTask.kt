package dev.robocode.tankroyale.gui.util

import java.awt.EventQueue

object GuiTask {

    fun enqueue(runnable: () -> Unit) {
        BusyPointer.activate()
        EventQueue.invokeLater {
            runnable.invoke()
            BusyPointer.deactivate()
        }
    }
}