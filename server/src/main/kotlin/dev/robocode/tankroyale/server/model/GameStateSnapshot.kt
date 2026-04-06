package dev.robocode.tankroyale.server.model

/**
 * Immutable snapshot of game state returned from [dev.robocode.tankroyale.server.core.ModelUpdater.update].
 *
 * Callers receive a snapshot that captures the relevant game-state values at the
 * end of a turn. The internal mutable [GameState] remains private to ModelUpdater.
 */
data class GameStateSnapshot(
    /** The last (current) round, or null before the first round starts. */
    val lastRound: MutableRound?,
    /** Whether the game has ended. */
    val isGameEnded: Boolean,
)
