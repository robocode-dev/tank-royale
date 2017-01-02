package net.robocode2.game;

import net.robocode2.model.Position;

public final class MathUtil {

	public static final double NEAR_DELTA = 0.00001;

	/**
	 * Normalizes an angle to a relative angle. The normalized angle will be in the range from -180 to 180, where 180
	 * itself is not included.
	 *
	 * @param angle
	 *            the angle to normalize
	 * @return the normalized angle that will be in the range of [-180,180[
	 */
	public static double normalRelativeAngleDegrees(double angle) {
		return (angle %= 360) >= 0 ? ((angle < 180) ? angle : (angle - 360))
				: ((angle >= -180) ? angle : (angle + 360));
	}

	/**
	 * Tests if the two {@code double} values are near to each other. It is recommended to use this method instead of
	 * testing if the two doubles are equal using an this expression: {@code value1 == value2}. The reason being, that
	 * this expression might never become {@code true} due to the precision of double values. Whether or not the
	 * specified doubles are near to each other is defined by the following expression:
	 * {@code (Math.abs(value1 - value2) < .00001)}
	 *
	 * @param value1
	 *            the first double value
	 * @param value2
	 *            the second double value
	 * @return {@code true} if the two doubles are near to each other; {@code false} otherwise.
	 */
	public static boolean isNear(double value1, double value2) {
		return (Math.abs(value1 - value2) < NEAR_DELTA);
	}

	public static double distance(Position p1, Position p2) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		return Math.hypot(dx, dy);
	}
}