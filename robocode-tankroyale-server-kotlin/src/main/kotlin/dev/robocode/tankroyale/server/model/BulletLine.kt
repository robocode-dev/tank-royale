package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Line
import dev.robocode.tankroyale.server.math.Point

/** Convenient class to wrap a bullet to cache the current and next position of the bullet. */
data class BulletLine(
    /** The bullet of the bullet line */
    val bullet: Bullet
) {
    private val bulletCopy = bullet.copy()

    /** Start position of the bullet line */
    private val start: Point by lazy { bulletCopy.position }

    /** End position of the bullet line */
    val end: Point by lazy { bulletCopy.nextPosition }

    /** The current bullet line */
    val line by lazy { Line(start, end) }
}