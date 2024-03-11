package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model

/**
 * Defines an initial starting position for a bot used for debugging only.
 * @param x X coordinate of starting position. If `null` a random value will be used.
 * @param y Y coordinate of starting position. If `null` a random value will be used.
 * @param direction Shared starting direction of the body, gun, and radar. If `null` a random value will be used.
 */
data class InitialPosition(val x: Double?, val y: Double?, val direction: Double?)