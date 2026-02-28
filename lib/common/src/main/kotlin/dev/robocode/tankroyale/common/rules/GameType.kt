package dev.robocode.tankroyale.common.rules

/** Game type identifiers used across server, GUI, and API modules. */
enum class GameType(val displayName: String) {
    CUSTOM("custom"),
    CLASSIC("classic"),
    MELEE("melee"),
    ONE_VS_ONE("1v1");

    companion object {
        fun from(displayName: String): GameType {
            entries.forEach { if (it.displayName == displayName) return it }
            throw IllegalArgumentException("Unknown game type display name: '$displayName'")
        }
    }
}
