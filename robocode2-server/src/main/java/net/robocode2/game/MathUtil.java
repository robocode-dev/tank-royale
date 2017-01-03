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
		return Math.hypot((p2.x - p1.x), (p2.y - p1.y));
	}

	// http://gigglingcorpse.com/2015/06/25/line-segment-intersection/
	public static boolean doLinesIntersect(Position a1, Position a2, Position b1, Position b2) {

		// Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III"
		// book (http://www.graphicsgems.org/)
		double ax = a2.x - a1.x;
		double ay = a2.y - a1.y;
		double bx = b1.x - b2.x;
		double by = b1.y - b2.y;
		double cx = a1.x - b1.x;
		double cy = a1.y - b1.y;

		double alphaNumerator = by * cx - bx * cy;
		double commonDenominator = ay * bx - ax * by;
		if (commonDenominator > 0) {
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator)
				return false;
		} else if (commonDenominator < 0) {
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator)
				return false;
		}
		double betaNumerator = ax * cy - ay * cx;
		if (commonDenominator > 0) {
			if (betaNumerator < 0 || betaNumerator > commonDenominator)
				return false;
		} else if (commonDenominator < 0) {
			if (betaNumerator > 0 || betaNumerator < commonDenominator)
				return false;
		}
		if (commonDenominator == 0) {
			// This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
			// The lines are parallel.
			// Check if they're collinear.
			double y3LessY1 = b1.y - a1.y;
			// see http://mathworld.wolfram.com/Collinear.html
			double collinearityTestForP3 = a1.x * (a2.y - b1.y) + a2.x * (y3LessY1) + b1.x * (a1.y - a2.y);
			// If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
			if (collinearityTestForP3 == 0) {
				// The lines are collinear. Now check if they overlap.
				if (a1.x >= b1.x && a1.x <= b2.x || a1.x <= b1.x && a1.x >= b2.x || a2.x >= b1.x && a2.x <= b2.x
						|| a2.x <= b1.x && a2.x >= b2.x || b1.x >= a1.x && b1.x <= a2.x
						|| b1.x <= a1.x && b1.x >= a2.x) {
					if (a1.y >= b1.y && a1.y <= b2.y || a1.y <= b1.y && a1.y >= b2.y || a2.y >= b1.y && a2.y <= b2.y
							|| a2.y <= b1.y && a2.y >= b2.y || b1.y >= a1.y && b1.y <= a2.y
							|| b1.y <= a1.y && b1.y >= a2.y) {
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}
}