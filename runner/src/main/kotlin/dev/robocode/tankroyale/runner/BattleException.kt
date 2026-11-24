package dev.robocode.tankroyale.runner

/**
 * Thrown when a battle cannot start or fails to complete.
 *
 * @param message human-readable description of the failure
 * @param cause the underlying exception, if any
 */
class BattleException @JvmOverloads constructor(
    message: String,
    cause: Throwable? = null,
    /** The bots that had not connected when the battle could not start, in time or at all; empty for every other failure. */
    val notConnected: List<BotIdentity> = emptyList(),
) : RuntimeException(message, cause)
