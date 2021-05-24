package dev.robocode.tankroyale.gui.settings

import java.lang.IllegalArgumentException

enum class GameType(val displayName: String) {
    CUSTOM("custom"),
    CLASSIC("classic"),
    MELEE("melee"),
    ONE_VS_ONE("1v1");

    companion object {
        fun from(displayName: String): GameType {
            values().forEach { if (it.displayName == displayName) return it }

            throw IllegalArgumentException("displayName was not found")
        }
    }
}