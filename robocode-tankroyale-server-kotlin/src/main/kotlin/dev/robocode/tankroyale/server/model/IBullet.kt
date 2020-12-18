package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.rules.calcBulletSpeed
import kotlin.math.cos
import kotlin.math.sin

interface IBullet {
    /** Id of the bot that fired this bullet */
    val botId: BotId

    /** Id of the bullet */
    val bulletId: BulletId

    /** Power of the bullet */
    val power: Double

    /** Direction of the bullet in degrees */
    val direction: Double

    /** Color of the bullet */
    val color: Color?

    /** Start position where the bullet was fired from */
    val startPosition: Point // must be immutable as this point is used for calculating future positions

    /** Tick, which is the number of turns since the bullet was fired */
    val tick: Int

    /** Speed of the bullet */
    fun speed(): Double = calcBulletSpeed(power)

    /** Current position */
    fun position(): Point = calcPosition()

    /** Next position */
    fun nextPosition(): Point = calcPosition(true)

    /**
     * Calculates the position of a bullet.
     * @param isNextPosition set to `true` to calc next position or `false` tp calc current position.
     * @return the calculated bullet position.
     */
    private fun calcPosition(isNextPosition: Boolean = false): Point {
        val tick = if (isNextPosition) tick + 1 else tick

        val angle = Math.toRadians(direction)
        val distance = speed() * tick
        val x = startPosition.x + cos(angle) * distance
        val y = startPosition.y + sin(angle) * distance
        return Point(x, y)
    }
}