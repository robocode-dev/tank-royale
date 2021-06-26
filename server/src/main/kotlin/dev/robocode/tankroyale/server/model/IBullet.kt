package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.calcBulletSpeed
import kotlin.math.cos
import kotlin.math.sin

/**
 * A bullet interface.
 * The [startPosition] and [tick] defines the current position of the bullet.
 */
interface IBullet {
    /** Unique id of the bullet. */
    val id: BulletId

    /** Unique id of the bot that fired this bullet. */
    val botId: BotId

    /** Power of the bullet. */
    val power: Double

    /** Direction of the bullet in degrees. */
    val direction: Double

    /** Color of the bullet. */
    val color: Color?

    /** Start position where the bullet was fired from. */
    val startPosition: Point // must be immutable as this point is used for calculating future positions

    /** Tick, which is the number of turns since the bullet was fired. */
    val tick: Int

    /**
     * Returns the speed of this bullet depending on the bullet power.
     * @return the speed of this bullet based on [calcBulletSpeed].
     * @see [calcBulletSpeed]
     */
    fun speed(): Double = calcBulletSpeed(power)

    /**
     * Returns the current position of this bullet based on [startPosition] and [tick].
     * @return a [Point] with containing the current position.
     */
    fun position(): Point = calcPosition()

    /**
     * Returns the next position of this bullet based on [startPosition] and [tick] + 1.
     * @return a [Point] with containing the next position.
     */
    fun nextPosition(): Point = calcPosition(true)

    /**
     * Calculates the current position of this bullet based on [startPosition] and [tick].
     * @param calcNextPosition set to `true` to calc next position or `false` to calc current position.
     * @return a [Point] with containing the current or next position.
     */
    private fun calcPosition(calcNextPosition: Boolean = false): Point {
        val tick = if (calcNextPosition) tick + 1 else tick

        val angle = Math.toRadians(direction)
        val distance = speed() * tick
        val x = startPosition.x + cos(angle) * distance
        val y = startPosition.y + sin(angle) * distance
        return Point(x, y)
    }
}