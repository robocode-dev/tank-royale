package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.calcBulletSpeed
import kotlin.math.cos
import kotlin.math.sin

/**
 * An immutable bullet.
 * The [startPosition] and [tick] defines the current position of the bullet.
 * @param id Unique id of the bullet.
 * @param botId Unique id of the bot that fired this bullet.
 * @param power Power of the bullet.
 * @param direction Direction of the bullet in degrees.
 * @param color Color of the bullet. If set to `null`, the default bullet color will be used.
 * @param startPosition Start position where the bullet was fired from.
 * @param tick Tick, which is the number of turns since the bullet was fired.
 */
data class Bullet(
    val id: BulletId,
    val botId: BotId,
    val power: Double,
    val direction: Double,
    val color: Color?,
    val startPosition: Point, // must be immutable as this point is used for calculating future positions
    val tick: Int = 0,
) {
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

    /**
     * Returns a hash code that is the (unique) id of this bullet making this call fast.
     * @return the (unique) id of this bullet.
     * @see [Object.hashCode]
     */
    override fun hashCode(): Int {
        return id.value
    }

    /**
     * Compares this bullet another object by checking if the input object is a [Bullet] instance and share the same
     * bullet id.
     * @param other is any object.
     * @return `true` if the two bullets are equal; `false` otherwise.
     * @see [Object.equals]
     */
    override fun equals(other: Any?): Boolean {
        return other is Bullet && other.id == id
    }
}
