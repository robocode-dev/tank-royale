package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.MainFrame
import java.awt.Color
import java.awt.Cursor
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JFrame

object BusyCursor {

    private const val TIMER_DELAY = 500L // millis

    private var count = AtomicInteger(0)

    // Invisible frame that will be put in front of the main window containing a busy pointer
    // The invisible frame in the top "disables" all user input to the main window.
    private val invisibleFrame = JFrame().apply {
        cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        isAlwaysOnTop = true
        isUndecorated = true
        isVisible = false
        background = Color(0, 0, 0, 0)
    }

    private var timer: Timer? = null

    fun activate() {
        if (count.getAndIncrement() == 0) { // first active?

            // Run a timer with a delay that shows the invisible frame with the busy pointer after a delay
            timer = Timer().apply {
                schedule(TimerTaskUtil.createTimerTask {
                    invisibleFrame.apply {
                        bounds = MainFrame.bounds
                        invisibleFrame.isVisible = true
                    }
                }, TIMER_DELAY)
            }
        }
    }

    fun deactivate() {
        if (count.decrementAndGet() == 0) { // last deactivate?

            // Cancel the current timer
            timer?.cancel()

            // Remove the invisible frame containing the busy pointer
            invisibleFrame.isVisible = false
        }
    }
}
