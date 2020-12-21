package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.INITIAL_BOT_ENERGY
import dev.robocode.tankroyale.server.rules.INITIAL_GUN_HEAT

/** Immutable Bot instance. */
data class Bot(
    /** Bot id */
    override val id: BotId,

    /** Energy level */
    override val energy: Double = INITIAL_BOT_ENERGY,

    /** Position (x, y) */
    override val position: Point,

    /** Driving direction in degrees */
    override val direction: Double,

    /** Gun direction in degrees */
    override val gunDirection: Double,

    /** Radar direction in degrees */
    override val radarDirection: Double,

    /** Radar spread angle in degrees */
    override val radarSpreadAngle: Double = 0.0,

    /** Speed */
    override val speed: Double = 0.0,

    /** Turn rate */
    override val turnRate: Double = 0.0,

    /** Gun turn rate */
    override val gunTurnRate: Double = 0.0,

    /** Radar turn rate */
    override val radarTurnRate: Double = 0.0,

    /** Gun heat */
    override val gunHeat: Double = INITIAL_GUN_HEAT,

    /** Body color */
    override val bodyColor: Color? = null,

    /** Gun turret color */
    override val turretColor: Color? = null,

    /** Radar color */
    override val radarColor: Color? = null,

    /** Bullet color */
    override val bulletColor: Color? = null,

    /** Scan color */
    override val scanColor: Color? = null,

    /** Tracks color */
    override val tracksColor: Color? = null,

    /** Gun color */
    override val gunColor: Color? = null,

    /** Scan direction in degrees */
    override val scanDirection: Double = radarDirection,

    /** Scan angle in degrees */
    override val scanSpreadAngle: Double = radarSpreadAngle,

    ) : IBot {
    /** X coordinate */
    override val x: Double
        get() = position.x

    /** Y coordinate */
    override val y: Double
        get() = position.y
}