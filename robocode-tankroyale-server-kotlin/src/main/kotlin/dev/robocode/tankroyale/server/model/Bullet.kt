package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Point
import kotlin.math.cos
import kotlin.math.sin

data class Bullet(
    /** Id of the bot that fired this bullet  */
    var botId: Int = 0,

    /** Id of the bullet  */
    var bulletId: Int = 0,

    /** Power of the bullet  */
    var power: Double = 0.0,

    /** X coordinate of the position where the bullet was fired from  */
    var startX: Double = 0.0,
    /** Y coordinate of the position where the bullet was fired from  */
    var startY: Double = 0.0,

    /** Direction of the bullet in degrees  */
    var direction: Double = 0.0,

    /** Tick, which is the number of turns since the bullet was fired  */
    var tick: Int = 0,

    /** Color of the bullet  */
    var color: Int? = null,
) {
    /** Speed of the bullet */
    val speed: Double get() = RuleMath.calcBulletSpeed(power)

    /**
     * Calculates the current bullet position based on the fire position and current tick.
     * @return the calculated bullet position.
     */
    fun calcPosition(): Point {
        return calcPosition(startX, startY, direction, speed, tick)
    }

    /**
     * Calculates the next bullet position based on the fire position and current tick.
     * @return the calculated bullet position.
     */
    fun calcNextPosition(): Point {
        return calcPosition(startX, startY, direction, speed, tick + 1)
    }

    /**
     * Calculates the position of a bullet.
     * @param startX is the x coordinate of the position where the bullet was fired from.
     * @param startY is the y coordinate of the position where the bullet was fired from.
     * @param direction is the direction of the bullet.
     * @param speed is the speed of the bullet.
     * @param tick is the number of turns since the bullet was fired.
     * @return the calculated bullet position.
     */
    private fun calcPosition(startX: Double, startY: Double, direction: Double, speed: Double, tick: Int): Point {
        val angle = Math.toRadians(direction)
        val distance = speed * tick
        val x = startX + cos(angle) * distance
        val y = startY + sin(angle) * distance
        return Point(x, y)
    }
}