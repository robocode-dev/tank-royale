package net.robocode2.model;

public final class Physics {

	public static final double INITIAL_BOT_ENERGY = 100.0;
	public static final double INITIAL_GUN_HEAT = 3.0;
	public static final double RADAR_RADIUS = 1200.0;
	public static final double ACCELLERATION = 1.0;
	public static final double DECELERATION = -2.0;
	public static final double MAX_FORWARD_SPEED = 8.0;
	public static final double MAX_REVERSE_SPEED = -8.0;

	public static double calcNewSpeed(double currentSpeed, double targetSpeed) {
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

	public static double calcGunHeat(double firePower) {
		return 1 + (firePower / 5);
	}
}
