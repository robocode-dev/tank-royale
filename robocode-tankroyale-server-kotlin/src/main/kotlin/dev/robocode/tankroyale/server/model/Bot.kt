package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.math.isNearTo
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

    /** Position (x, y) */
    var position: Point,

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
    var bodyColor: Color? = null,

    /** Gun turret color */
    var turretColor: Color? = null,

    /** Radar color */
    var radarColor: Color? = null,

    /** Bullet color */
    var bulletColor: Color? = null,

    /** Scan color */
    var scanColor: Color? = null,

    /** Tracks color */
    var tracksColor: Color? = null,

    /** Gun color */
    var gunColor: Color? = null,

    /** Score record */
    var score: Score,

    /** Scan direction in degrees */
    var scanDirection: Double = radarDirection,

    /** Scan angle in degrees */
    var scanSpreadAngle: Double = radarSpreadAngle,
) {
    /** X coordinate */
    inline var x: Double
        get() = position.x
        set(value) { position.x = value }

    /** Y coordinate */
    inline var y: Double
        get() = position.y
        set(value) { position.y = value }

    /** Check if bot is alive */
    private inline val isAlive: Boolean get() = energy >= 0

    /** Check if bot is dead */
    inline val isDead: Boolean get() = energy < 0

    /** Check if bot is disabled (cannot move) */
    private inline val isDisabled: Boolean get() = isAlive && energy.isNearTo(0.0)

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
        moveToNewPosition(speed)
    }

    /**
     * Moves bot backwards due to bouncing.
     * @param distance is the distance to bounce back.
     */
    fun bounceBack(distance: Double) {
        moveToNewPosition(if (speed > 0) -distance else distance)
    }

    /**
     * Move to new position based on the current position, the driving direction, and distance to move.
     * @param distance is the distance to move.
     */
    private fun moveToNewPosition(distance: Double) {
        val angle = Math.toRadians(direction)
        x += cos(angle) * distance
        y += sin(angle) * distance
    }
}