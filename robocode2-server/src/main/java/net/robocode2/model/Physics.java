package net.robocode2.model;

public final class Physics {

	public static final int BOT_BOUNDING_CIRCLE_DIAMETER = 36;
	public static final int BOT_BOUNDING_CIRCLE_RADIUS = 36 / 2;
	public static final double INITIAL_BOT_ENERGY = 100.0;
	public static final double INITIAL_GUN_HEAT = 3.0;
	public static final double ACCELLERATION = 1.0;
	public static final double DECELERATION = -2.0;
	public static final double MAX_FORWARD_SPEED = 8.0;
	public static final double MAX_REVERSE_SPEED = -8.0;
	public static final double RADAR_RADIUS = 1200.0;
	public static final double MIN_BULLET_POWER = 0.1;
	public static final double MAX_BULLET_POWER = 3.0;

	public static final double MAX_BULLET_SPEED = calcBulletSpeed(MIN_BULLET_POWER);

	public static double calcBotSpeed(double currentSpeed, double targetSpeed) {
		double delta = targetSpeed - currentSpeed;
		if (currentSpeed >= 0) {
			if (delta >= 0) {
				double step = (delta >= ACCELLERATION) ? ACCELLERATION : delta;
				return Math.min(currentSpeed + step, MAX_FORWARD_SPEED);
			} else {
				double step = (delta <= DECELERATION) ? DECELERATION : delta;
				return Math.max(currentSpeed + step, MAX_REVERSE_SPEED);
			}
		} else {
			if (delta < 0) {
				double step = (-delta >= ACCELLERATION) ? ACCELLERATION : -delta;
				return Math.max(currentSpeed - step, -MAX_FORWARD_SPEED);
			} else {
				double step = (-delta <= DECELERATION) ? DECELERATION : -delta;
				return Math.min(currentSpeed - step, -MAX_REVERSE_SPEED);
			}
		}
	}

	public static double calcWallDamage(double speed) {
		return Math.max(Math.abs(speed) / 2 - 1, 0);
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
