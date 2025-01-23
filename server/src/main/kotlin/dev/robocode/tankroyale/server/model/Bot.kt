package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.INITIAL_BOT_ENERGY
import dev.robocode.tankroyale.server.rules.INITIAL_GUN_HEAT

/**
 * Immutable Bot instance.
 * @param id Unique id of this bot.
 * @param energy Energy level.
 * @param position Position (x, y).
 * @param direction Driving direction in degrees.
 * @param gunDirection Gun direction in degrees.
 * @param radarDirection Radar direction in degrees.
 * @param radarSpreadAngle Radar spread angle in degrees.
 * @param speed Speed.
 * @param turnRate Bot turn rate.
 * @param gunTurnRate Gun turn rate.
 * @param radarTurnRate Radar turn rate.
 * @param gunHeat Gun heat.
 * @param bodyColor Body color.
 * @param turretColor Gun turret color.
 * @param radarColor Radar color.
 * @param bulletColor Bullet color.
 * @param scanColor Scan arc color.
 * @param tracksColor Tracks color.
 * @param gunColor Gun color.
 * @param isDebuggingEnabled Flag indicating if graphical debugging is enabled.
 * @param debugGraphics Graphics used for rendering debugging graphics, if enabled
 */
data class Bot(
    override val id: BotId,
    override val isDroid: Boolean = false,
    override val sessionId: String?,
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
    override val stdOut: String? = null,
    override val stdErr: String? = null,
    override val isDebuggingEnabled: Boolean = false,
    override val debugGraphics: String? = null,
    override val teammateIds: Set<BotId> = HashSet(),
) : IBot {

    /** X coordinate which is a shortcut for `position.x`. */
    override val x: Double
        get() = position.x

    /** Y coordinate which is a shortcut for `position.y`. */
    override val y: Double
        get() = position.y
}