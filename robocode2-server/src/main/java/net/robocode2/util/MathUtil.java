package net.robocode2.util;

import net.robocode2.model.Point;

/**
 * Math utility class used for geometric methods.
 * 
 * @author Flemming N. Larsen
 */
public final class MathUtil {

	/**
	 * Normalizes an angle to an absolute angle into the range [0,360[
	 *
	 * @param angle
	 *            the angle to normalize
	 * @return the normalized absolute angle
	 */
	public static double normalAbsoluteDegrees(double angle) {
		return (angle %= 360) >= 0 ? angle : (angle + 360);
	}

	/**
	 * Normalizes an angle to an relative angle into the range [-180,180[
	 *
	 * @param angle
	 *            the angle to normalize
	 * @return the normalized relative angle.
	 */
	public static double normalRelativeDegrees(double angle) {
		return (angle %= 360) >= 0 ? ((angle < 180) ? angle : (angle - 360))
				: ((angle >= -180) ? angle : (angle + 360));
	}

	/**
	 * Tests if the two {@code double} values are nearly equal. It is recommended to use this method instead of testing
	 * if the two doubles are equal using an expression like this: {@code value1 == value2}. The reason being, that this
	 * expression might never become {@code true} due to the precision of double values. Whether or not, the specified
	 * doubles are close enough to be considered as equal, is defined by the following expression:
	 * {@code abs(value1 - value2) < epsilon}, where epsilon is defined to be 1E-6.
	 *
	 * @param value1
	 *            the first double value
	 * @param value2
	 *            the second double value
	 * @return {@code true} if the two doubles are near to each other; {@code false} otherwise.
	 */
	public static boolean nearlyEqual(double value1, double value2) {
		return (Math.abs(value1 - value2) < 1E-6);
	}

	/**
	 * Returns the shortest distance between two points: sqrt(dx*dx + dy*dy).
	 * 
	 * @param p1
	 *            is one point
	 * @param p2
	 *            is another point
	 * @return the distance between the two points
	 */
	public static double distance(Point p1, Point p2) {
		return Math.hypot((p2.x - p1.x), (p2.y - p1.y));
	}

	/**
	 * Checks if a line segment defined by the two points (x1,y1) and (x2,y2) is intersecting the circle defined by the
	 * center point (cx,cy) and radius, r.
	 * <p>
	 * The algorithm used in this method is based on Jeff Thompson's collision detection method for line/circle:<br>
	 * http://www.jeffreythompson.org/collision-detection/line-circle.php
	 * 
	 * @param x1
	 *            is the x coordinate of 1st point of the line segment
	 * @param y1
	 *            is the y coordinate of 1st point of the line segment
	 * @param x2
	 *            is the x coordinate of 2nd point of the line segment
	 * @param y2
	 *            is the y coordinate of 2nd point of the line segment
	 * @param cx
	 *            is the x coordinate of the center point of the circle
	 * @param cy
	 *            is the y coordinate of the center point of the circle
	 * @param r
	 *            is the radius of the circle
	 * @return {@code true} if the line is intersecting the circle; {@code false} otherwise.
	 */
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

		// Check that the closest point is on the line segment and inside the circle
		return isPointOnLine(closestX, closestY, x1, y1, x2, y2) && isPointInsideCircle(closestX, closestY, cx, cy, r);
	}

	/**
	 * Checks if a point defined by (x1,y1) is inside or on the circle defined by the center point (cx,cy) and radius,
	 * r.
	 * <p>
	 * The algorithm used in this method is based on the Pythagorean Theorem:<br>
	 * http://www.jeffreythompson.org/collision-detection/point-circle.php
	 *
	 * @param px
	 *            is the x coordinate of the point
	 * @param py
	 *            is the y coordinate of the point
	 * @param cx
	 *            is the x coordinate of the center point of the circle
	 * @param cy
	 *            is the y coordinate of the center point of the circle
	 * @param r
	 *            is the radius of the circle
	 * @return {@code true} if the point is inside or on the circle; {@code false} otherwise.
	 */
	public static boolean isPointInsideCircle(double px, double py, double cx, double cy, double r) {
		double dx = px - cx;
		double dy = py - cy;

		// If the distance is less or equal than the circle's radius the point is considered to be inside the circle
		return ((dx * dx) + (dy * dy)) <= (r * r);
	}

	/**
	 * Checks if the point defined by (px,py) is on the line segment defined by the two points (x1,y1) and (x2,y2).
	 * <p>
	 * The algorithm first checks if the "cross-product" between the vectors (x1,y1) -> (px,py) and (x1,y1) -> (x2,y2)
	 * is equal to 0. If true, the point lies on the line. Secondly, the algorithm makes sure that the point is points
	 * of the line segment.
	 * <p>
	 * The algorithm is described by AnT here:<br>
	 * https://stackoverflow.com/questions/11907947/how-to-check-if-a-point-lies-on-a-line-between-2-other-points/11908158#11908158
	 * 
	 * @param px
	 *            is the x coordinate of the point
	 * @param py
	 *            is the y coordinate of the point
	 * @param x1
	 *            is the x coordinate of 1st point of the line segment
	 * @param y1
	 *            is the y coordinate of 1st point of the line segment
	 * @param x2
	 *            is the x coordinate of 2nd point of the line segment
	 * @param y2
	 *            is the y coordinate of 2nd point of the line segment
	 * @return {@code true} if the point is on the line segment; {@code false} otherwise.
	 */
	public static boolean isPointOnLine(double px, double py, double x1, double y1, double x2, double y2) {
		// Calculate cross product of vectors
		double dxp = px - x1;
		double dyp = py - y1;

		double dxl = x2 - x1;
		double dyl = y2 - y1;

		double cross = dxp * dyl - dyp * dxl;

		// Point lies on the line, if and only if, cross-product is equal to zero.
		if (!nearlyEqual(cross, 0)) {
			return false;
		}

		// Check whether it lies between the original points
		if (Math.abs(dxl) >= Math.abs(dyl)) {
			return dxl > 0 ? (x1 <= px && px <= x2) : (x2 <= px && px <= x1);
		} else {
			return dyl > 0 ? (y1 <= py && py <= y2) : (y2 <= py && py <= y1);
		}
	}

	/**
	 * Checks if two line segments (a and b) are intersecting. Line segment a is defined by the two points a1 and a2,
	 * and line segment b is defined by the two points b1 and b2.
	 * <p>
	 * The algorithm used in this method is based on CommanderKeith's algorithm:<br>
	 * http://www.java-gaming.org/index.php?topic=22590.0 <br>
	 * http://gigglingcorpse.com/2015/06/25/line-segment-intersection/
	 * <p>
	 * His algorithm is based on based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems
	 * III". Keith Woodward added new code to optimize Franklin's original code.
	 * 
	 * @param a1
	 *            is point a1, 1st point of line segment a
	 * @param a2
	 *            is point a2, 2nd point of line segment a
	 * @param b1
	 *            is point b1, 1st point of line segment b
	 * @param b2
	 *            is point b2, 2nd point of line segment b
	 * @return {@code true} if the two lines are intersecting; {@code false} otherwise.
	 */
	public static boolean isLineIntersectingLine(Point a1, Point a2, Point b1, Point b2) {

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

	/**
	 * Checks if a circle is intersecting/inside a circle sector. The circle sector is defined by a sector center,
	 * sector radius, a arc start angle, and arc end angle. The arc end angle must be greater that the arc start angle.
	 * <p>
	 * The algorithm used in this method is based on Oren Trutner algorithm:
	 * http://stackoverflow.com/questions/13652518/efficiently-find-points-inside-a-circle-sector
	 * 
	 * @param circleCenter
	 *            is the center point of the circle
	 * @param circleRadius
	 *            is the radius of the circle
	 * @param sectorCenter
	 *            is the center point of the circle sector
	 * @param sectorRadius
	 *            is the radius of the circle sector
	 * @param arcStartAngle
	 *            is the arc start angle in degrees
	 * @param arcEndAngle
	 *            is the arc end angle in degrees
	 * @return {@code true} if the circle lines is intersecting/inside the circle segment; {@code false} otherwise.
	 */
	public static boolean isCircleIntersectingCircleSector(Point circleCenter, double circleRadius, Point sectorCenter,
			double sectorRadius, double arcStartAngle, double arcEndAngle) {

		double maxRadiusToPoint = sectorRadius + circleRadius;

		double vx = circleCenter.x - sectorCenter.x;
		double vy = circleCenter.y - sectorCenter.y;

		// Check if point is outside max radius to point
		if (((vx * vx) + (vy * vy)) > (maxRadiusToPoint * maxRadiusToPoint)) {
			return false; // outside radius
		}

		double arcStartRad = Math.toRadians(arcStartAngle);
		double arcEndRad = Math.toRadians(arcEndAngle);

		double dx = Math.cos(arcStartRad) * sectorRadius;
		double dy = Math.sin(arcStartRad) * sectorRadius;
		Point arcStart = new Point(dx, dy);

		dx = Math.cos(arcEndRad) * sectorRadius;
		dy = Math.sin(arcEndRad) * sectorRadius;
		Point arcEnd = new Point(dx, dy);

		// Check if circle center is within the circle sector arms
		if (isClockwise(arcStart.x, arcStart.y, vx, vy) && !isClockwise(arcEnd.x, arcEnd.y, vx, vy)) {
			return true;
		}

		// Check if circle is intersecting one of the arms
		return isLineIntersectingCircle(sectorCenter.x, sectorCenter.y, sectorCenter.x + arcStart.x,
				sectorCenter.y + arcStart.y, circleCenter.x, circleCenter.y, circleRadius)
				|| isLineIntersectingCircle(sectorCenter.x, sectorCenter.y, sectorCenter.x + arcEnd.x,
						sectorCenter.y + arcEnd.y, circleCenter.x, circleCenter.y, circleRadius);
	}

	/**
	 * Checks if vector v1 is clockwise to vector v2 compared to a shared starting point.
	 *
	 * @param v1_x
	 *            is the x coordinate of vector v1
	 * @param v1_y
	 *            is the y coordinate of vector v1
	 * @param v2_x
	 *            is the x coordinate of vector v2
	 * @param v2_y
	 *            is the y coordinate of vector v2
	 * @return {@code true} if v1 is clockwise to v2; {@code false} otherwise.
	 * 
	 */
	public static boolean isClockwise(double v1_x, double v1_y, double v2_x, double v2_y) {
		return -v1_x * v2_y + v1_y * v2_x > 0;
	}

	/**
	 * Returns the shortest distance from a point (px,py) to the line segment defined by the two points (x1,y1) and
	 * (x2,y2).
	 * 
	 * @param px
	 *            is the x coordinate of the point
	 * @param py
	 *            is the y corrdinate of the point
	 * @param x1
	 *            is the x coordinate of the 1st point of the line segment
	 * @param y1
	 *            is the y coordinate of the 1st point of the line segment
	 * @param x2
	 *            is the x coordinate of the 2nd point of the line segment
	 * @param y2
	 *            is the y coordinate of the 2nd point of the line segment
	 * @return the shortest distance from the point to the line segment
	 */
	public static double distanceToLine(double px, double py, double x1, double y1, double x2, double y2) {

		// Get the squared length of the line
		double dx = x2 - x1;
		double dy = y2 - y1;
		double len2 = (dx * dx) + (dy * dy);

		// Get dot product of the line and circle
		double dot = (((px - x1) * dx) + ((py - y1) * dy)) / len2;

		// Find the closest point on the line from the point
		double closestX = x1 + (dot * dx);
		double closestY = y1 + (dot * dy);

		// Return distance
		return Math.sqrt(closestX * closestX + closestY * closestY);
	}

	/**
	 * Returns nearest point to a line segment, defined by the two points (x1,y1) and (x2,y2), from a point (px,py). The
	 * nearest point might be located on the line segment itself, or outside the line segment, but still on the line
	 * defined by the line segment. The distance between the specified point (px,py) and the nearest point on the line
	 * will be the shortest distance between the point (px,py) and the line segment.
	 * 
	 * @param px
	 *            is the x coordinate of the point
	 * @param py
	 *            is the y coordinate of the point
	 * @param x1
	 *            is the x coordinate of the 1st point of the line segment
	 * @param y1
	 *            is the y coordinate of the 1st point of the line segment
	 * @param x2
	 *            is the x coordinate of the 2nd point of the line segment
	 * @param y2
	 *            is the y coordinate of the 2nd point of the line segment
	 * @return the nearest point to a line segment from the specified point
	 */
	public static Point nearestPointToLine(double px, double py, double x1, double y1, double x2, double y2) {

		// Get the squared length of the line
		double dx = x2 - x1;
		double dy = y2 - y1;
		double len2 = (dx * dx) + (dy * dy);

		// Get dot product of the line and circle
		double dot = (((px - x1) * dx) + ((py - y1) * dy)) / len2;

		// Find the closest point on the line from the point
		double closestX = x1 + (dot * dx);
		double closestY = y1 + (dot * dy);

		// Return closest point on the line
		return new Point(closestX, closestY);
	}

	/**
	 * Returns a random direction in the range 0 up to 360 degrees.
	 * 
	 * @return direction in degrees in the range [0;360[ degrees.
	 */
	public static double randomDirection() {
		return Math.random() * 360;
	}
}