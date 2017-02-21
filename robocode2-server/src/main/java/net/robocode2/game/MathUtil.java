package net.robocode2.game;

import net.robocode2.model.Position;

public final class MathUtil {

	public static final double NEAR_DELTA = 0.00001;

	/**
	 * Normalizes an angle to an absolute angle. The normalized angle will be in the range from 0 to 360, where 360
	 * itself is not included.
	 *
	 * @param angle
	 *            the angle to normalize
	 * @return the normalized angle that will be in the range of [0,360[
	 */
	public static double normalAbsoluteAngleDegrees(double angle) {
		return (angle %= 360) >= 0 ? angle : (angle + 360);
	}

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

	// LINE/CIRCLE
	// http://www.jeffreythompson.org/collision-detection/line-circle.php
	public static boolean isLineIntersectingCircle(double x1, double y1, double x2, double y2, double cx, double cy,
			double r) {

		// Check if one of the line ends is within the circle
		if (isPointInsideCircle(x1, y1, cx, cy, r) || isPointInsideCircle(x2, y2, cx, cy, r)) {
			return true;
		}

		// Get the squared length of the line
		double dx = x2 - x1;
		double dy = y2 - y1;
		double len2 = (dx * dx) + (dy * dy);

		// Get dot product of the line and circle
		double dot = (((cx - x1) * dx) + ((cy - y1) * dy)) / len2;

		// Find the closest point on the line from the circle
		double closestX = x1 + (dot * dx);
		double closestY = y1 + (dot * dy);

		// Check if the closest point is on the line segment and the point is inside the circle
		return isPointOnLine(x1, y1, x2, y2, closestX, closestY) && isPointInsideCircle(closestX, closestY, cx, cy, r);
	}

	// POINT/CIRCLE
	public static boolean isPointInsideCircle(double px, double py, double cx, double cy, double r) {
		double dx = px - cx;
		double dy = py - cy;

		// If the distance is less than the circle's radius the point is inside!
		return ((dx * dx) + (dy * dy)) <= (r * r);
	}

	// LINE/POINT
	public static boolean isPointOnLine(double x1, double y1, double x2, double y2, double px, double py) {
		// Calculate cross product of vectors
		double dxp = px - x1;
		double dyp = py - y1;

		double dxl = x2 - x1;
		double dyl = y2 - y1;

		double cross = dxp * dyl - dyp * dxl;

		// point lies on the line if and only if cross is equal to zero.
		if (!isNear(cross, 0)) {
			return false;
		}

		// Check whether it lies between the original points
		if (Math.abs(dxl) >= Math.abs(dyl)) {
			return dxl > 0 ? (x1 <= px && px <= x2) : (x2 <= px && px <= x1);
		} else {
			return dyl > 0 ? (y1 <= py && py <= y2) : (y2 <= py && py <= y1);
		}
	}

	// LINE/LINE
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
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator) {
				return false;
			}
		} else if (commonDenominator < 0) {
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator) {
				return false;
			}
		}
		double betaNumerator = ax * cy - ay * cx;
		if (commonDenominator > 0) {
			if (betaNumerator < 0 || betaNumerator > commonDenominator) {
				return false;
			}
		} else if (commonDenominator < 0) {
			if (betaNumerator > 0 || betaNumerator < commonDenominator) {
				return false;
			}
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