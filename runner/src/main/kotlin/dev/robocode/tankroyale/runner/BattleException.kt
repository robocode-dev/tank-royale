package dev.robocode.tankroyale.runner

/**
 * Thrown when a battle cannot start or fails to complete.
 *
 * @param message human-readable description of the failure
 * @param cause the underlying exception, if any
 */
class BattleException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
