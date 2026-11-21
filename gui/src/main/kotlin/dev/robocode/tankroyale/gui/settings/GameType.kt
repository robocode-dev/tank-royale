package dev.robocode.tankroyale.gui.settings

enum class GameType(val displayName: String) {
    CUSTOM("custom"),
    CLASSIC("classic"),
    MELEE("melee"),
    ONE_VS_ONE("1v1"),
    TWIN_DUEL("twinduel");

    companion object {
        /**
         * The game types the desktop program's local server accepts: every one New Battle offers, so a battle of any of
         * them, TwinDuel too, is not refused by the server it is started on.
         */
        fun serverGamesArgument(): String = entries.joinToString(",") { it.displayName }

        fun from(displayName: String): GameType {
            entries.forEach { if (it.displayName == displayName) return it }

            throw IllegalArgumentException("'displayName' was not found")
        }
    }
}
