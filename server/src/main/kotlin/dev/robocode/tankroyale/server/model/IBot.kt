package dev.robocode.tankroyale.server.model

/** Bot interface. */
interface IBot {
    /** Bot id */
    val id: BotId

    /** Teammate ids */
    val teammateIds: Set<BotId>

    /** Flag specifying if the bot is a droid */
    val isDroid: Boolean

    /** Session id */
    val sessionId: String?

    /** Energy level */
    val energy: Double

    /** Position (x, y) */
    val position: IPoint

    /** X coordinate */
    val x: Double

    /** Y coordinate */
    val y: Double

    /** Driving direction in degrees */
    val direction: Double

    /** Gun direction in degrees */
    val gunDirection: Double

    /** Radar direction in degrees */
    val radarDirection: Double

    /** Radar spread angle in degrees */
    val radarSpreadAngle: Double

    /** Speed */
    val speed: Double

    /** Turn rate */
    val turnRate: Double

    /** Gun turn rate */
    val gunTurnRate: Double

    /** Radar turn rate */
    val radarTurnRate: Double

    /** Gun heat */
    val gunHeat: Double

    /** Body color */
    val bodyColor: Color?

    /** Gun turret color */
    val turretColor: Color?

    /** Radar color */
    val radarColor: Color?

    /** Bullet color */
    val bulletColor: Color?

    /** Scan color */
    val scanColor: Color?

    /** Tracks color */
    val tracksColor: Color?

    /** Gun color */
    val gunColor: Color?

    /** Standard output (last data) */
    val stdOut: String?

    /** Standard error (last data) */
    val stdErr: String?

    /** Flag indicating if debug graphics for this bot are sent to the GUI */
    val isDebuggingEnabled: Boolean

    /** Debug graphics as SVG */
    val debugGraphics: String?

    /** Check if bot is alive */
    val isAlive: Boolean get() = energy >= 0

    /** Check if bot is dead */
    val isDead: Boolean get() = !isAlive

    /** Check if bot is enabled (can move) */
    val isEnabled: Boolean get() = isAlive && energy.isNotNearTo(.0)

    /** Check if bot is disabled (cannot move) */
    val isDisabled: Boolean get() = isAlive && energy.isNearTo(.0)
}