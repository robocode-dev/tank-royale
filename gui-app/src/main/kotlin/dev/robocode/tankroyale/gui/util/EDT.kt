package dev.robocode.tankroyale.gui.util

import java.awt.EventQueue

object EDT {

    fun enqueue(callable: () -> Unit) {
        EventQueue.invokeLater {
            BusyCursor.activate()
            try {
                callable.invoke()
            } finally {
                BusyCursor.deactivate()
            }
        }
    }
}