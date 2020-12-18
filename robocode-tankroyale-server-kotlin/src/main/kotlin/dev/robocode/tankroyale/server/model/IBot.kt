package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.math.IPoint
import dev.robocode.tankroyale.server.math.isNearTo
import dev.robocode.tankroyale.server.math.isNotNearTo

/** Bot interface. */
interface IBot {
    /** Bot id */
    val id: BotId

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

    /** Scan direction in degrees */
    val scanDirection: Double

    /** Scan angle in degrees */
    val scanSpreadAngle: Double

    /** Check if bot is alive */
    val isAlive: Boolean get() = energy >= 0

    /** Check if bot is dead */
    val isDead: Boolean get() = !isAlive

    /** Check if bot is disabled (cannot move) */
    val isDisabled: Boolean get() = isAlive && energy.isNearTo(0.0)

    /** Check if bot is enabled (can move) */
    val isEnabled: Boolean get() = isAlive && energy.isNotNearTo(0.0)
}