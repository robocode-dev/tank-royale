package dev.robocode.tankroyale.gui.ui.control

import dev.robocode.tankroyale.common.event.On
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.player.ReplayBattlePlayer
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.components.RcSlider
import dev.robocode.tankroyale.gui.ui.components.SkullComponent
import java.awt.EventQueue
import java.util.*
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicSliderUI

/**
 * Slider component for showing battle progress. This component allows users to
 * seek specific positions in a replay by dragging the slider.
 */
object ProgressSlider : RcSlider() {
    /**
     * SafeSliderUI prevents NPE from BasicSliderUI when labelTable is null but paintLabels is true.
     * This handles edge cases during UI updates where the state might be temporarily inconsistent.
     */
    private class SafeSliderUI : BasicSliderUI {
        constructor(slider: ProgressSlider) : super(slider)

        override fun calculateTrackBuffer() {
            try {
                super.calculateTrackBuffer()
            } catch (_: NullPointerException) {
                // Prevent crash if labelTable is null but we're trying to calculate label geometry
                trackBuffer = 0
            }
        }

        override fun calculateGeometry() {
            try {
                super.calculateGeometry()
            } catch (_: NullPointerException) {
                // Handle NPE during geometry recalculation by using default geometry
                trackBuffer = 0
            }
        }
    }

    /**
     * LinkedDictionary wraps LinkedHashMap to preserve insertion order
     * while implementing the Dictionary interface required by JSlider.
     */
    @Suppress("UNCHECKED_CAST")
    private class LinkedDictionary<K, V> : Dictionary<K, V>() {
        private val map = LinkedHashMap<K, V>()

        override fun size(): Int = map.size
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun keys(): Enumeration<K> = Collections.enumeration(map.keys)
        override fun elements(): Enumeration<V> = Collections.enumeration(map.values)
        override fun get(key: Any): V? = map[key as? K]
        override fun put(key: K, value: V): V? = map.put(key, value)
        override fun remove(key: Any): V? = map.remove(key as? K)
    }

    // Default maximum value for the slider
    private const val DEFAULT_MAX_VALUE = 100

    // Reference to the current replay player
    private var currentReplayPlayer: ReplayBattlePlayer? = null

    // Flag to prevent recursive updates when programmatically changing the slider value
    private var updatingProgrammatically = false

    override fun updateUI() {
        // Use our safe UI delegate that handles null labelTable gracefully
        setUI(SafeSliderUI(this))
    }

    init {
        minimum = 0
        maximum = DEFAULT_MAX_VALUE
        value = 0

        paintTicks = true
        paintLabels = false  // Start with false to avoid NPE when labelTable is null
        toolTipText = Hints.get("control.progress")

        // Initially hide the progress slider until we know the player supports seeking
        isVisible = false

        ClientEvents.onPlayerChanged+= On(ProgressSlider) { player ->
            EventQueue.invokeLater {
                // Reset to a default scale when player changes
                value = 0

                // If this is a replay player, subscribe to its events
                if (player is ReplayBattlePlayer) {
                    isVisible = true
                    currentReplayPlayer = player
                    maximum = maxOf(player.getTotalRounds() - 1, 0)
                    player.onReplayEvent+= On(ProgressSlider) { eventIndex ->
                        updateProgress(eventIndex)
                    }
                    val labelTable = LinkedDictionary<Int, JComponent>()
                    player.getDeathMarkers()
                        .sortedWith(compareBy<Pair<Int, Boolean>> { it.second }.thenBy { it.first })
                        .forEach { (pos, roundEnd) -> labelTable.put(pos, SkullComponent(if (roundEnd) 1.0f else 0.6f)) }
                    this.labelTable = labelTable
                    paintLabels = true
                } else {
                    isVisible = false
                    currentReplayPlayer = null
                    maximum = DEFAULT_MAX_VALUE
                    paintLabels = false
                    this.labelTable = null
                }
                // Revalidate and repaint to apply changes safely
                revalidate()
                repaint()
            }
        }
        ClientEvents.onConnected+= On(ProgressSlider) {
            setEnabled(true)
        }
        ClientEvents.onGameAborted+= On(ProgressSlider) {
            setEnabled(false)
        }
        ClientEvents.onGameEnded+= On(ProgressSlider) {
            setEnabled(false)
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
     * Updates the progress value within the min / max range
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
