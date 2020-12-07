package dev.robocode.tankroyale.server.model

/** Game state */
data class GameState(
    /** Arena */
    val arena: Arena,

    /** List of rounds */
    val rounds: MutableList<Round> = ArrayList(),

    /** Flag specifying if game has ended yet */
    var gameEnded: Boolean,
) {
    /** Last round */
    val lastRound: Round? get() = if(rounds.isNotEmpty()) rounds[rounds.size - 1] else null
}