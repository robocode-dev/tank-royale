package dev.robocode.tankroyale.server.model

/**
 * Mutable state of a round in a battle.
 *
 * This class is used internally as an accumulator for the turns in a round.
 */
data class MutableRound(
    /** Round number */
    var roundNumber: Int,

    /** List of turns */
    val turns: MutableList<ITurn> = mutableListOf(),

    /** Flag specifying if round has ended yet */
    var roundEnded: Boolean = false,
) {
    /** Last turn */
    val lastTurn: ITurn? get() = if (turns.isNotEmpty()) turns[turns.size - 1] else null
}
