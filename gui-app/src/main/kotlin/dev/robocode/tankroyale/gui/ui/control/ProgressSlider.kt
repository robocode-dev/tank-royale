package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.player.BattlePlayerFeature
import dev.robocode.tankroyale.gui.player.ReplayBattlePlayer
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.components.RcSlider
import java.awt.EventQueue

/**
 * Slider component for showing battle progress. This is a dummy component for now,
 * without any control bindings.
 */
object ProgressSlider : RcSlider() {

    val onProgressChanged = Event<Int>()

    // Default maximum value for the slider
    private const val DEFAULT_MAX_VALUE = 100

    init {
        minimum = 0
        maximum = DEFAULT_MAX_VALUE
        value = 0

        paintTicks = false
        paintLabels = false

        toolTipText = Hints.get("control.progress")

        // Initially hide the progress slider until we know the player supports seeking
        isVisible = false

        // Subscribe to player changes to update visibility based on seek capability
        ClientEvents.onPlayerChanged.subscribe(ProgressSlider) { player ->
            EventQueue.invokeLater {
                isVisible = player.supportsFeature(BattlePlayerFeature.SEEK)

                // Reset to default scale when player changes
                value = 0

                // If this is a replay player, it will fire the onBattleInfoChanged event
                // which we'll handle separately
                if (player is ReplayBattlePlayer) {
                    maximum = maxOf(player.getEventCount() - 1, 0)
                    player.onReplayEvent.subscribe(ProgressSlider) { eventIndex ->
                        updateProgress(eventIndex)
                    }
                } else {
                    maximum = DEFAULT_MAX_VALUE
                }
            }
        }

        // Subscribe to tick events to update slider position during playback
        ClientEvents.onTickEvent.subscribe(ProgressSlider) { tickEvent ->

        }

        // Add change listener for future functionality
        addChangeListener {
            if (!valueIsAdjusting) {
                onProgressChanged.fire(value)
            }
        }
    }

    /**
     * Updates the progress value within min/max range
     */
    fun setProgress(progress: Int) {
        value = progress.coerceIn(minimum, maximum)
    }

    /**
     * Updates the slider position based on the current tick event
     */
    private fun updateProgress(eventIndex: Int) {
        EventQueue.invokeLater {
            // Only update if we're not currently being adjusted by the user
            if (!valueIsAdjusting) {
                setProgress(eventIndex)
            }
        }
    }
}
