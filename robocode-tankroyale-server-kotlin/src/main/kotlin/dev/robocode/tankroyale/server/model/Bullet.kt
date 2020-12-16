package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.rules.calcBulletSpeed
import kotlin.math.cos
import kotlin.math.sin

data class Bullet(
    /** Id of the bot that fired this bullet */
    val botId: BotId,

    /** Id of the bullet */
    val bulletId: BulletId,

    /** Power of the bullet */
    val power: Double,

    /** Direction of the bullet in degrees */
    val direction: Double,

    /** Start position where the bullet was fired from */
    private val startPosition: Point, // must be immutable as this point is used for calculating future positions

    /** Tick, which is the number of turns since the bullet was fired */
    private var tick: Int = 0,

    /** Color of the bullet */
    val color: Color?,
) {
    /** Speed of the bullet */
    val speed by lazy { calcBulletSpeed(power) }

    /** Increment the tick used for moving the bullet when calculating its position. */
    fun incrementTick() {
        tick++
    }

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
        val distance = speed * tick
        val x = startPosition.x + cos(angle) * distance
        val y = startPosition.y + sin(angle) * distance
        return Point(x, y)
    }

    override fun hashCode(): Int {
        return bulletId.value
    }

    override fun equals(other: Any?): Boolean {
        return other is Bullet && other.bulletId == bulletId
    }
}