package dev.robocode.tankroyale.server.rules

import kotlin.math.abs
import kotlin.math.sign

/**
 * Calculates new bot speed.
 * @param currentSpeed is the current speed.
 * @param targetSpeed is the target speed.
 * @return is the calculated new speed of the bot.
 */
fun calcNewBotSpeed(currentSpeed: Double, targetSpeed: Double): Double {
    val delta = targetSpeed - currentSpeed
    return if (currentSpeed >= 0) {
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
fun limitTurnRate(turnRate: Double, speed: Double): Double =
    sign(turnRate) * abs(turnRate).coerceAtMost(calcMaxTurnRate(speed))

/**
 * Limits the gun turn rate.
 * @param gunTurnRate is the gun turn rate to limit
 * @return limited gun turn rate.
 */
fun limitGunTurnRate(gunTurnRate: Double): Double =
    sign(gunTurnRate) * abs(gunTurnRate).coerceAtMost(MAX_GUN_TURN_RATE)

/**
 * Limits the radar turn rate.
 * @param radarTurnRate is the radar turn rate to limit
 * @return limited radar turn rate.
 */
fun limitRadarTurnRate(radarTurnRate: Double): Double =
    sign(radarTurnRate) * abs(radarTurnRate).coerceAtMost(MAX_RADAR_TURN_RATE)


/**
 * Calculates the maximum driving turn rate for a specific speed.
 * @param speed is the speed that limits the driving turn rate.
 * @return maximum turn rate.
 */
fun calcMaxTurnRate(speed: Double): Double = MAX_TURN_RATE - 0.75 * abs(speed)

/**
 * Calculates wall damage.
 * @param speed is the speed of the bot hitting the wall.
 * @return wall damage.
 */
fun calcWallDamage(speed: Double): Double = (abs(speed) / 2 - 1).coerceAtLeast(0.0)

/**
 * Calculates bullet speed.
 * @param firepower is the firepower used for firing the bullet.
 * @return bullet speed.
 */
fun calcBulletSpeed(firepower: Double): Double = 20 - 3 * firepower

/**
 * Calculates bullet damage.
 * @param firepower is the firepower used for firing the bullet.
 * @return bullet damage.
 */
fun calcBulletDamage(firepower: Double): Double {
    var damage = 4 * firepower
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
fun calcGunHeat(firepower: Double): Double = 1 + firepower / 5