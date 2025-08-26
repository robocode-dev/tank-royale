package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.player.BattlePlayerFeature
import dev.robocode.tankroyale.gui.player.ReplayBattlePlayer
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.components.RcSlider
import java.awt.EventQueue

/**
 * Slider component for showing battle progress. This component allows users to
 * seek to specific positions in a replay by dragging the slider.
 */
object ProgressSlider : RcSlider() {

    // Default maximum value for the slider
    private const val DEFAULT_MAX_VALUE = 100

    // Reference to the current replay player
    private var currentReplayPlayer: ReplayBattlePlayer? = null

    // Flag to prevent recursive updates when programmatically changing the slider value
    private var updatingProgrammatically = false

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

                // If this is a replay player, subscribe to its events
                if (player is ReplayBattlePlayer) {
                    currentReplayPlayer = player
                    maximum = maxOf(player.getEventCount() - 1, 0)
                    player.onReplayEvent.subscribe(ProgressSlider) { eventIndex ->
                        updateProgress(eventIndex)
                    }
                } else {
                    currentReplayPlayer = null
                    maximum = DEFAULT_MAX_VALUE
                }
            }
        }

        // Add change listener to handle user interaction
        addChangeListener {
            // Only handle user-initiated changes, not programmatic ones
            if (!updatingProgrammatically) {
                if (valueIsAdjusting) {
                    // When the user starts dragging, pause the replay
                    currentReplayPlayer?.pause()
                } else {
                    // When the user finishes dragging, seek to the selected position
                    currentReplayPlayer?.seekToTurn(value)
                }
            }
        }
    }

    /**
     * Updates the progress value within min/max range
     */
    fun setProgress(progress: Int) {
        // Set flag to prevent recursive updates
        updatingProgrammatically = true
        try {
            value = progress.coerceIn(minimum, maximum)
        } finally {
            updatingProgrammatically = false
        }
    }

    /**
     * Updates the slider position based on the current event index
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
