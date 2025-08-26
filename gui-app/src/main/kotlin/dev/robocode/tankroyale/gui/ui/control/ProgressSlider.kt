package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.components.RcSlider

/**
 * Slider component for showing battle progress. This is a dummy component for now,
 * without any control bindings.
 */
object ProgressSlider : RcSlider() {

    val onProgressChanged = Event<Int>()

    init {
        minimum = 0
        maximum = 100
        value = 0

        paintTicks = false
        paintLabels = false

        toolTipText = Hints.get("control.progress")

        // Add change listener for future functionality
        addChangeListener { 
            if (!valueIsAdjusting) {
                onProgressChanged.fire(value)
            }
        }
    }

    /**
     * Updates the progress value (0-100)
     */
    fun setProgress(progress: Int) {
        value = progress.coerceIn(minimum, maximum)
    }
}
