package dev.robocode.tankroyale.botapi

/**
 * Constants.
 */
object Constants {

    /** The radius of the bounding circle of the bot. */
    const val BOUNDING_CIRCLE_RADIUS: Int = 18

    /** The radius of the radar's scan beam. */
    const val SCAN_RADIUS: Int = 1200

    /** Max possible driving turn rate (deg/turn). */
    const val MAX_TURN_RATE: Int = 10

    /** Max gun turn rate (deg/turn). */
    const val MAX_GUN_TURN_RATE: Int = 20

    /** Max radar turn rate (deg/turn). */
    const val MAX_RADAR_TURN_RATE: Int = 45

    /** Max absolute speed (units/turn). */
    const val MAX_SPEED: Int = 8

    /** Min firepower. */
    const val MIN_FIREPOWER: Double = 0.1

    /** Max firepower. */
    const val MAX_FIREPOWER: Double = 3.0

    /** Min bullet speed (units/turn). */
    val MIN_BULLET_SPEED: Double = 20 - 3 * MAX_FIREPOWER

    /** Max bullet speed (units/turn). */
    val MAX_BULLET_SPEED: Double = 20 - 3 * MIN_FIREPOWER

    /** Acceleration (units/turn^2). */
    const val ACCELERATION: Int = 1

    /** Deceleration (units/turn^2). */
    const val DECELERATION: Int = -2
}
