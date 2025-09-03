package dev.robocode.tankroyale.gui.player

/**
 * Contains information about a battle replay that is useful for UI components like progress sliders.
 * This data is provided when a battle player loads a replay file.
 */
data class BattleInfo(
    /** Total number of turns in the battle */
    val totalEvents: Int,
)
