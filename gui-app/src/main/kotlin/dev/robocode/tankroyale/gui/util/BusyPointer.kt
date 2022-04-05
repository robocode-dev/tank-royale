package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.MainWindow
import java.awt.Cursor

object BusyPointer {

    private var count: Int = 0

    fun activate() {
        count++
        MainWindow.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    }

    fun deactivate() {
        if (--count == 0)
            MainWindow.cursor = Cursor.getDefaultCursor()
    }
}