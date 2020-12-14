package dev.robocode.tankroyale.server.model

/** State of a round in a battle. */
data class Round(
    /** Round number */
    var roundNumber: Int,

    /** List of turns */
    val turns: MutableList<Turn> = mutableListOf(),

    /** Flag specifying if round has ended yet */
    var roundEnded: Boolean = false,
) {
    /** Last turn */
    val lastTurn: Turn? get() = if (turns.isNotEmpty()) turns[turns.size - 1] else null
}