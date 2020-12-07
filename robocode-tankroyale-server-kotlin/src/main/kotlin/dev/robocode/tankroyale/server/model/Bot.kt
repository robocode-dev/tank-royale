package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.math.nearlyEqual
import dev.robocode.tankroyale.server.rules.INITIAL_BOT_ENERGY
import dev.robocode.tankroyale.server.rules.INITIAL_GUN_HEAT
import kotlin.math.cos
import kotlin.math.sin

/** Bot instance. */
data class Bot(
    /** Bot id */
    var id: BotId,

    /** Energy level */
    var energy: Double = INITIAL_BOT_ENERGY,

    /** X coordinate on the arena */
    var x: Double,

    /** Y coordinate on the arena */
    var y: Double,

    /** Driving direction in degrees */
    var direction: Double,

    /** Gun direction in degrees */
    var gunDirection: Double,

    /** Radar direction in degrees */
    var radarDirection: Double,

    /** Radar spread angle in degrees */
    var radarSpreadAngle: Double = 0.0,

    /** Speed */
    var speed: Double = 0.0,

    /** Turn rate */
    var turnRate: Double = 0.0,

    /** Gun turn rate */
    var gunTurnRate: Double = 0.0,

    /** Radar turn rate */
    var radarTurnRate: Double = 0.0,

    /** Gun heat */
    var gunHeat: Double = INITIAL_GUN_HEAT,

    /** Body color */
    var bodyColor: Int? = null,

    /** Gun turret color */
    var turretColor: Int? = null,

    /** Radar color */
    var radarColor: Int? = null,

    /** Bullet color */
    var bulletColor: Int? = null,

    /** Scan color */
    var scanColor: Int? = null,

    /** Tracks color */
    var tracksColor: Int? = null,

    /** Gun color */
    var gunColor: Int? = null,

    /** Score record */
    var score: Score,

    /** Scan direction in degrees */
    var scanDirection: Double = radarDirection,

    /** Scan angle in degrees */
    var scanSpreadAngle: Double = radarSpreadAngle,
) {
    /** Check if bot is alive */
    inline val isAlive: Boolean get() = energy >= 0

    /** Check if bot is dead */
    inline val isDead: Boolean get() = energy < 0

    /** Check if bot is disabled (cannot move) */
    inline val isDisabled: Boolean get() = isAlive && nearlyEqual(energy, 0.0)

    /** Check if bot is enabled (can move) */
    val isEnabled: Boolean = !isDisabled

    /**
     * Adds damage to the bot.
     * @param damage is the damage done to this bot.
     * @return `true` if the bot got killed due to the damage, `false` otherwise.
     */
    fun addDamage(damage: Double): Boolean {
        val aliveBefore = isAlive
        energy -= damage
        return isDead && aliveBefore
    }

    /**
     * Change the energy level.
     * @param deltaEnergy is the delta energy to add to the current energy level,
     * which can be both positive and negative.
     */
    fun changeEnergy(deltaEnergy: Double) {
        energy += deltaEnergy
    }

    /**
     * Moves bot to the new (next turn) position based on the current position, the driving direction, and the speed.
     */
    fun moveToNewPosition() {
        val (x, y) = calcNewPosition(direction, speed)
        this.x = x
        this.y = y
    }

    /**
     * Moves bot backwards due to bouncing.
     * @param distance is the distance to bounce back.
     */
    fun bounceBack(distance: Double) {
        val (x, y) = calcNewPosition(direction, if (speed > 0) -distance else distance)
        this.x = x
        this.y = y
    }

    /**
     * Calculate the (next turn) position based on the current position, the driving direction, and the speed.
     * @param direction is the new driving direction.
     * @param distance is the distance to move.
     * @return the calculated new position of the bot.
     */
    private fun calcNewPosition(direction: Double, distance: Double): Point {
        val angle = Math.toRadians(direction)
        val x: Double = this.x + cos(angle) * distance
        val y: Double = this.y + sin(angle) * distance
        return Point(x, y)
    }
}