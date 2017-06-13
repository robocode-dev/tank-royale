package net.robocode2.model;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;
import static net.robocode2.model.IRuleConstants.ACCELLERATION;
import static net.robocode2.model.IRuleConstants.DECELERATION;
import static net.robocode2.model.IRuleConstants.MAX_FORWARD_SPEED;
import static net.robocode2.model.IRuleConstants.MAX_GUN_TURN_RATE;
import static net.robocode2.model.IRuleConstants.MAX_RADAR_TURN_RATE;
import static net.robocode2.model.IRuleConstants.MAX_REVERSE_SPEED;
import static net.robocode2.model.IRuleConstants.MAX_TURN_RATE;

public final class RuleMath {

	public static double calcBotSpeed(double currentSpeed, double targetSpeed) {
		double delta = targetSpeed - currentSpeed;
		if (currentSpeed >= 0) {
			if (delta >= 0) {
				double step = (delta >= ACCELLERATION) ? ACCELLERATION : delta;
				return min(currentSpeed + step, MAX_FORWARD_SPEED);
			} else {
				double step = (delta <= DECELERATION) ? DECELERATION : delta;
				return max(currentSpeed + step, MAX_REVERSE_SPEED);
			}
		} else {
			if (delta < 0) {
				double step = (-delta >= ACCELLERATION) ? ACCELLERATION : -delta;
				return max(currentSpeed - step, -MAX_FORWARD_SPEED);
			} else {
				double step = (-delta <= DECELERATION) ? DECELERATION : -delta;
				return min(currentSpeed - step, -MAX_REVERSE_SPEED);
			}
		}
	}

	public static double limitTurnRate(double turnRate, double speed) {
		return signum(turnRate) * min(abs(turnRate), calcMaxTurnRate(speed));
	}

	public static double limitGunTurnRate(double gunTurnRate) {
		return signum(gunTurnRate) * min(abs(gunTurnRate), MAX_GUN_TURN_RATE);
	}

	public static double limitRadarTurnRate(double radarTurnRate) {
		return signum(radarTurnRate) * min(abs(radarTurnRate), MAX_RADAR_TURN_RATE);
	}

	public static double calcMaxTurnRate(double speed) {
		return MAX_TURN_RATE - 0.75 * abs(speed);
	}

	public static double calcScanAngle(double turnRate) {
		return max(-MAX_RADAR_TURN_RATE, min(MAX_RADAR_TURN_RATE, turnRate));
	}

	public static double calcWallDamage(double speed) {
		return max(abs(speed) / 2 - 1, 0);
	}

	public static double calcBulletSpeed(double firepower) {
		return 20 - 3 * firepower;
	}

	public static double calcBulletDamage(double firepower) {
		double damage = 4 * firepower;
		if (firepower > 1) {
			damage += 2 * (firepower - 1);
		}
		return damage;
	}

	public static double calcGunHeat(double firepower) {
		return 1 + (firepower / 5);
	}
}
