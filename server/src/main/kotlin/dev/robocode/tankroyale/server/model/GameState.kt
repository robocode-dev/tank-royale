package dev.robocode.tankroyale.server.model

/** Game state */
data class GameState(
    /** Arena */
    val arenaSize: Arena,

    /** List of rounds */
    val rounds: MutableList<IRound> = mutableListOf(),

    /** Flag specifying if game has ended yet */
    var isGameEnded: Boolean = false,
) {
    /** Last round */
    val lastRound: IRound? get() = if (rounds.isNotEmpty()) rounds[rounds.size - 1] else null
}