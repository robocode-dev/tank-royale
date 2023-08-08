package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.MainFrame
import java.awt.Color
import java.awt.Cursor
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JFrame

object BusyCursor {

    private var count = AtomicInteger(0)

    private val invisibleFrame = JFrame().apply {
        cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        isAlwaysOnTop = true
        isUndecorated = true
        isVisible = true

        background = Color(0, 0, 0, 0)
    }

    fun activate() {
        if (count.getAndIncrement() == 0) {
            invisibleFrame.apply {
                bounds = MainFrame.bounds
                invisibleFrame.isVisible = true
            }
        }
    }

    fun deactivate() {
        if (count.decrementAndGet() == 0) {
            invisibleFrame.isVisible = false
        }
    }
}
