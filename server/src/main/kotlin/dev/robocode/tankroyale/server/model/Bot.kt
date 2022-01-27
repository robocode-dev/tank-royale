package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.INITIAL_BOT_ENERGY
import dev.robocode.tankroyale.server.rules.INITIAL_GUN_HEAT

/**
 * Immutable Bot instance.
 * @property id Unique id of this bot.
 * @property energy Energy level.
 * @property position Position (x, y).
 * @property direction Driving direction in degrees.
 * @property gunDirection Gun direction in degrees.
 * @property radarDirection Radar direction in degrees.
 * @property radarSpreadAngle Radar spread angle in degrees.
 * @property speed Speed.
 * @property turnRate Bot turn rate.
 * @property gunTurnRate Gun turn rate.
 * @property radarTurnRate Radar turn rate.
 * @property gunHeat Gun heat.
 * @property bodyColor Body color.
 * @property turretColor Gun turret color.
 * @property radarColor Radar color.
 * @property bulletColor Bullet color.
 * @property scanColor Scan arc color.
 * @property tracksColor Tracks color.
 * @property gunColor Gun color.
 * @property scanDirection Scan direction in degrees.
 * @property scanSpreadAngle Scan angle in degrees.
 */
data class Bot(
    override val id: BotId,
    override val energy: Double = INITIAL_BOT_ENERGY,
    override val position: Point, // immutable point
    override val direction: Double,
    override val gunDirection: Double,
    override val radarDirection: Double,
    override val radarSpreadAngle: Double = 0.0,
    override val speed: Double = 0.0,
    override val turnRate: Double = 0.0,
    override val gunTurnRate: Double = 0.0,
    override val radarTurnRate: Double = 0.0,
    override val gunHeat: Double = INITIAL_GUN_HEAT,
    override val bodyColor: Color? = null,
    override val turretColor: Color? = null,
    override val radarColor: Color? = null,
    override val bulletColor: Color? = null,
    override val scanColor: Color? = null,
    override val tracksColor: Color? = null,
    override val gunColor: Color? = null,
    override val scanDirection: Double = radarDirection,
    override val scanSpreadAngle: Double = radarSpreadAngle,

    ) : IBot {

    /** X coordinate which is a shortcut for `position.x`. */
    override val x: Double
        get() = position.x

    /** Y coordinate which is a shortcut for `position.y`. */
    override val y: Double
        get() = position.y
}