package dev.robocode.tankroyale.server.rules

import kotlin.math.abs

/**
 * Returns value clamped to the inclusive range of min and max.
 *
 * @param value is the value to be clamped.
 * @param min   is the lower bound of the result.
 * @param max   is the upper bound of the result.
 * @return is the clamped value.
 */
fun clamp(value: Double, min: Double, max: Double): Double {
    return min.coerceAtLeast(max.coerceAtMost(value))
}

/**
 * Calculates new bot speed.
 * @param currentSpeed is the current speed.
 * @param targetSpeed is the target speed.
 * @return is the calculated new speed of the bot.
 */
fun calcNewBotSpeed(currentSpeed: Double, targetSpeed: Double): Double {
    val delta = targetSpeed - currentSpeed
    return if (currentSpeed == 0.0) {
        if (targetSpeed >= 0) {
            val step = delta.coerceAtMost(ACCELERATION)
            (currentSpeed + step).coerceAtMost(MAX_FORWARD_SPEED)
        } else {
            val step = (-delta).coerceAtMost(ACCELERATION)
            (currentSpeed - step).coerceAtLeast(MAX_BACKWARD_SPEED)
        }
    } else if (currentSpeed > 0) {
        if (delta >= 0) {
            val step = delta.coerceAtMost(ACCELERATION)
            (currentSpeed + step).coerceAtMost(MAX_FORWARD_SPEED)
        } else {
            val step = delta.coerceAtLeast(DECELERATION)
            (currentSpeed + step).coerceAtLeast(MAX_BACKWARD_SPEED)
        }
    } else {
        if (delta < 0) {
            val step = (-delta).coerceAtMost(ACCELERATION)
            (currentSpeed - step).coerceAtLeast(-MAX_FORWARD_SPEED)
        } else {
            val step = (-delta).coerceAtLeast(DECELERATION)
            (currentSpeed - step).coerceAtMost(-MAX_BACKWARD_SPEED)
        }
    }
}

/**
 * Limits the driving turn rate.
 * @param turnRate is the driving turn rate to limit
 * @param speed is the speed
 * @return limited driving turn rate.
 */
fun limitTurnRate(turnRate: Double, speed: Double): Double {
    val maxTurnRate = calcMaxTurnRate(speed)
    return clamp(turnRate, -maxTurnRate, maxTurnRate)
}

/**
 * Limits the gun turn rate.
 * @param gunTurnRate is the gun turn rate to limit
 * @return limited gun turn rate.
 */
fun limitGunTurnRate(gunTurnRate: Double): Double = clamp(gunTurnRate, -MAX_GUN_TURN_RATE, MAX_GUN_TURN_RATE)

/**
 * Limits the radar turn rate.
 * @param radarTurnRate is the radar turn rate to limit
 * @return limited radar turn rate.
 */
fun limitRadarTurnRate(radarTurnRate: Double): Double = clamp(radarTurnRate, -MAX_RADAR_TURN_RATE, MAX_RADAR_TURN_RATE)

/**
 * Calculates the maximum driving turn rate for a specific speed.
 * @param speed is the speed that limits the driving turn rate.
 * @return maximum turn rate.
 */
fun calcMaxTurnRate(speed: Double): Double = MAX_TURN_RATE - 0.75 * abs(clampSpeed(speed))

/**
 * Calculates wall damage.
 * @param speed is the speed of the bot hitting the wall.
 * @return wall damage.
 */
fun calcWallDamage(speed: Double): Double = (abs(clampSpeed(speed)) / 2 - 1).coerceAtLeast(0.0)

/**
 * Calculates bullet speed.
 * @param firepower is the firepower used for firing the bullet.
 * @return bullet speed.
 */
fun calcBulletSpeed(firepower: Double): Double = 20 - 3 * clampFirepower(firepower)

/**
 * Calculates bullet damage.
 * @param firepower is the firepower used for firing the bullet.
 * @return bullet damage.
 */
fun calcBulletDamage(firepower: Double): Double {
    var damage = 4 * clampFirepower(firepower)
    if (firepower > 1) {
        damage += 2 * (firepower - 1)
    }
    return damage
}

/**
 * Calculate gun heat after having fired the gun.
 * @param firepower is the firepower used for firing the bullet.
 * @return gun heat.
 */
fun calcGunHeat(firepower: Double): Double = 1 + clampFirepower(firepower) / 5

fun clampSpeed(speed: Double) = clamp(speed, MAX_BACKWARD_SPEED, MAX_FORWARD_SPEED)

fun clampFirepower(firepower: Double) = clamp(firepower, MIN_FIREPOWER, MAX_FIREPOWER)