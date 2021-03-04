package dev.robocode.tankroyale.server.model

/** Round interface */
interface IRound {
    /** Round number */
    val roundNumber: Int

    /** List of turns */
    val turns: List<ITurn>

    /** Flag specifying if round has ended yet */
    val roundEnded: Boolean

    /** Last turn */
    val lastTurn: ITurn? get() = if (turns.isNotEmpty()) turns[turns.size - 1] else null;
}
