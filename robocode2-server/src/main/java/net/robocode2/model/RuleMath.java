package net.robocode2.model;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static net.robocode2.model.RuleConstants.ACCELERATION;
import static net.robocode2.model.RuleConstants.DECELERATION;
import static net.robocode2.model.RuleConstants.MAX_FORWARD_SPEED;
import static net.robocode2.model.RuleConstants.MAX_GUN_TURN_RATE;
import static net.robocode2.model.RuleConstants.MAX_RADAR_TURN_RATE;
import static net.robocode2.model.RuleConstants.MAX_BACKWARD_SPEED;
import static net.robocode2.model.RuleConstants.MAX_TURN_RATE;

/**
 * Defines the rule math
 *
 * @author Flemming N. Larsen
 */
public final class RuleMath {
	
	private RuleMath() {}

	/**
	 * Calculates new bot speed
	 * 
	 * @param currentSpeed
	 *            is the current speed
	 * @param targetSpeed
	 *            is the target speed
	 * @return is the calculated new speed of the bot
	 */
	public static double calcNewBotSpeed(double currentSpeed, double targetSpeed) {
		double delta = targetSpeed - currentSpeed;
		if (currentSpeed >= 0) {
			if (delta >= 0) {
				double step = (delta >= ACCELERATION) ? ACCELERATION : delta;
				return min(currentSpeed + step, MAX_FORWARD_SPEED);
			} else {
				double step = (delta <= DECELERATION) ? DECELERATION : delta;
				return max(currentSpeed + step, MAX_BACKWARD_SPEED);
			}
		} else {
			if (delta < 0) {
				double step = (-delta >= ACCELERATION) ? ACCELERATION : -delta;
				return max(currentSpeed - step, -MAX_FORWARD_SPEED);
			} else {
				double step = (-delta <= DECELERATION) ? DECELERATION : -delta;
				return min(currentSpeed - step, -MAX_BACKWARD_SPEED);
			}
		}
	}

	/**
	 * Limits the driving turn rate
	 * 
	 * @param turnRate
	 *            is the driving turn rate to limit
	 * @param speed
	 *            is the speed
	 * @return limited driving turn rate
	 */
	public static double limitTurnRate(double turnRate, double speed) {
		return signum(turnRate) * min(abs(turnRate), calcMaxTurnRate(speed));
	}

	/**
	 * Limits the gun turn rate
	 * 
	 * @param gunTurnRate
	 *            is the gun turn rate to limit
	 * @return limited gun turn rate
	 */
	public static double limitGunTurnRate(double gunTurnRate) {
		return signum(gunTurnRate) * min(abs(gunTurnRate), MAX_GUN_TURN_RATE);
	}

	/**
	 * Limits the radar turn rate
	 * 
	 * @param radarTurnRate
	 *            is the radar turn rate to limit
	 * @return limited radar turn rate
	 */
	public static double limitRadarTurnRate(double radarTurnRate) {
		return signum(radarTurnRate) * min(abs(radarTurnRate), MAX_RADAR_TURN_RATE);
	}

	/**
	 * Calculates the maximum driving turn rate for a specific speed
	 * 
	 * @param speed
	 *            is the speed that limits the driving turn rate
	 * @return maximum turn rate
	 */
	public static double calcMaxTurnRate(double speed) {
		return MAX_TURN_RATE - 0.75 * abs(speed);
	}

	/**
	 * Calculates wall damage
	 * 
	 * @param speed
	 *            is the speed of the bot hitting the wall
	 * @return wall damage
	 */
	public static double calcWallDamage(double speed) {
		return max(abs(speed) / 2 - 1, 0);
	}

	/**
	 * Calculates bullet speed
	 * 
	 * @param firepower
	 *            is the firepower used for firing the bullet
	 * @return bullet speed
	 */
	public static double calcBulletSpeed(double firepower) {
		return 20 - 3 * firepower;
	}

	/**
	 * Calculates bullet damage
	 * 
	 * @param firepower
	 *            is the firepower used for firing the bullet
	 * @return bullet damage
	 */
	public static double calcBulletDamage(double firepower) {
		double damage = 4 * firepower;
		if (firepower > 1) {
			damage += 2 * (firepower - 1);
		}
		return damage;
	}

	/**
	 * Calculate gun heat after having fired the gun
	 * 
	 * @param firepower
	 *            is the firepower used for firing the bullet
	 * @return gun heat
	 */
	public static double calcGunHeat(double firepower) {
		return 1 + (firepower / 5);
	}
}
