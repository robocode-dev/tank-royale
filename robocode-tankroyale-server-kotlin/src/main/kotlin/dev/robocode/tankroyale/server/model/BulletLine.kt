package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Line
import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.model.Bullet

/** Convenient class to wrap a bullet to cache the current and next position of the bullet. */
class BulletLine(
    /** The bullet of the bullet line */
    val bullet: Bullet
) {
    /** Start position of the bullet line */
    private val start: Point by lazy { bullet.calcPosition() }

    /** End position of the bullet line */
    val end: Point by lazy { bullet.calcNextPosition() }

    /** The current bullet line */
    val line: Line by lazy { Line(start, end) }
}