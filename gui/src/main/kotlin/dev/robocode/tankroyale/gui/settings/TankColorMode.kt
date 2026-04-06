package dev.robocode.tankroyale.gui.settings

import java.util.*

enum class TankColorMode(val value: String) {
    BOT_COLORS("bot-colors"),
    BOT_COLORS_ONCE("bot-colors-once"),
    DEFAULT_COLORS("default-colors"),
    BOT_COLORS_WHEN_DEBUGGING("bot-colors-when-debugging");

    companion object {
        fun fromString(value: String?): TankColorMode {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: BOT_COLORS
        }
    }
}
