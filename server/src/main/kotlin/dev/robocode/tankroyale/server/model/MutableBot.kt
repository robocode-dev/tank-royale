package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.INITIAL_BOT_ENERGY
import dev.robocode.tankroyale.server.rules.INITIAL_GUN_HEAT
import kotlin.math.cos
import kotlin.math.sin

/** Mutable Bot instance. */
data class MutableBot(
    /** Bot id */
    override val id: BotId,

    /** Teammate ids */
    override val teammateIds: Set<BotId> = HashSet(),

    /** Flag specifying if the bot is a droid */
    override val isDroid: Boolean = false,

    /** Session id */
    override var sessionId: String? = null,

    /** Energy level */
    override var energy: Double = INITIAL_BOT_ENERGY,

    /** Position (x, y) */
    override var position: MutablePoint,

    /** Driving direction in degrees */
    override var direction: Double,

    /** Gun direction in degrees */
    override var gunDirection: Double,

    /** Radar direction in degrees */
    override var radarDirection: Double,

    /** Radar spread angle in degrees */
    override var radarSpreadAngle: Double = 0.0,

    /** Speed */
    override var speed: Double = 0.0,

    /** Turn rate */
    override var turnRate: Double = 0.0,

    /** Gun turn rate */
    override var gunTurnRate: Double = 0.0,

    /** Radar turn rate */
    override var radarTurnRate: Double = 0.0,

    /** Gun heat */
    override var gunHeat: Double = INITIAL_GUN_HEAT,

    /** Body color */
    override var bodyColor: Color? = null,

    /** Gun turret color */
    override var turretColor: Color? = null,

    /** Radar color */
    override var radarColor: Color? = null,

    /** Bullet color */
    override var bulletColor: Color? = null,

    /** Scan color */
    override var scanColor: Color? = null,

    /** Tracks color */
    override var tracksColor: Color? = null,

    /** Gun color */
    override var gunColor: Color? = null,

    /** Standard output (last data) */
    override var stdOut: String? = null,

    /** Standard error (last data) */
    override var stdErr: String? = null,

    /** Indicates if graphical debugging is enabled */
    override var isDebuggingEnabled: Boolean =  false,

    /** Debug graphics as an SVG string */
    override var debugGraphics: String? = null,

) : IBot {

    /** X coordinate */
    override var x: Double
        get() = position.x
        set(value) {
            position.x = value
        }

    /** Y coordinate */
    override var y: Double
        get() = position.y
        set(value) {
            position.y = value
        }

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
        // Move to new position
        val angle = Math.toRadians(direction)
        x += cos(angle) * speed
        y += sin(angle) * speed
    }
}