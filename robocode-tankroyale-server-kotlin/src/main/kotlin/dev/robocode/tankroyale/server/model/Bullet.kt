package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.rules.calcBulletSpeed
import kotlin.math.cos
import kotlin.math.sin

data class Bullet(
    /** Id of the bot that fired this bullet  */
    var botId: BotId,

    /** Id of the bullet  */
    var bulletId: Int,

    /** Power of the bullet  */
    var power: Double,

    /** X coordinate of the position where the bullet was fired from  */
    var startX: Double,

    /** Y coordinate of the position where the bullet was fired from  */
    var startY: Double,

    /** Direction of the bullet in degrees  */
    var direction: Double,

    /** Tick, which is the number of turns since the bullet was fired  */
    var tick: Int = 0,

    /** Color of the bullet  */
    var color: Int?,
) {
    /** Speed of the bullet */
    inline val speed: Double get() = calcBulletSpeed(power)

    /** Increment the tick used for moving the bullet when calculating its position. */
    fun incrementTick() { tick++ }

    /**
     * Calculates the current bullet position based on the fire position and current tick.
     * @return the calculated bullet position.
     */
    fun calcPosition(): Point = calcPosition(startX, startY, direction, speed, tick)

    /**
     * Calculates the next bullet position based on the fire position and current tick.
     * @return the calculated bullet position.
     */
    fun calcNextPosition(): Point = calcPosition(startX, startY, direction, speed, tick + 1)

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