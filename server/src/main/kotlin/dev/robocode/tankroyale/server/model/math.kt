package dev.robocode.tankroyale.server.model

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Normalizes an angle to an absolute angle into the range [0,360[
 * @param angle the angle to normalize.
 * @return the normalized absolute angle.
 */
fun normalizeAbsoluteDegrees(angle: Double): Double {
    val normalAngle = angle % 360
    return if (normalAngle >= 0) normalAngle else normalAngle + 360
}

/**
 * Normalizes an angle to an relative angle into the range [-180,180[
 *
 * @param angle
 * the angle to normalize
 * @return the normalized relative angle.
 */
fun normalizeRelativeDegrees(angle: Double): Double {
    val normalAngle = angle % 360
    return if (normalAngle >= 0) {
        if (normalAngle < 180) normalAngle else normalAngle - 360
    } else {
        if (normalAngle >= -180) normalAngle else normalAngle + 360
    }
}

/**
 * Tests if a `double` value is near to another 'double' value. It is recommended to use this method instead of testing
 * if the two doubles are equal using an expression like this: `value1 == value2`. The reason is that this expression
 * might never become `true` due to the precision of double values. Whether, the specified doubles are close enough to
 * be considered as equal, is defined by the following expression:
 * `abs(value1 - value2) < epsilon`, where epsilon is defined to be 1E-6.
 *
 * @param value the `double` value to compare to this double.
 * @return `true` if the `double` value is near to this `double`; `false` otherwise.
 */
fun Double.isNearTo(value: Double): Boolean {
    return abs(this - value) < 1E-6
}

/**
 * Tests if a `double` value is not near to another 'double' value. See Double.isNearTo(Double).
 *
 * @param value the `double` value to compare to this double.
 * @return `true` if the `double` value is not near to this `double`; `false` otherwise.
 */
fun Double.isNotNearTo(value: Double): Boolean {
    return !isNearTo(value)
}

/**
 * Checks if a line segment defined by the two points (x1,y1) and (x2,y2) is intersecting the circle defined by the
 * center point (cx,cy) and radius.
 *
 * The algorithm used in this method is based on Jeff Thompson's collision detection method for line/circle:<br>
 * <br>
 * http://www.jeffreythompson.org/collision-detection/line-circle.php
 *
 * @param line is line segment.
 * @param center is center point.
 * @param radius is the radius of the circle.
 * @return `true` if the line is intersecting the circle; `false` otherwise.
 */
fun isLineIntersectingCircle(line: Line, center: IPoint, radius: Double): Boolean {
    val x1 = line.start.x
    val y1 = line.start.y
    val x2 = line.end.x
    val y2 = line.end.y
    val cx = center.x
    val cy = center.y

    // Check if one of the line ends is within the circle
    if (isPointInsideCircle(line.start, center, radius) || isPointInsideCircle(line.end, center, radius)) {
        return true
    }

    // Get the squared length of the line
    val dx = x2 - x1
    val dy = y2 - y1
    val len2 = dx * dx + dy * dy

    // Get dot product of the line and circle
    val dot = ((cx - x1) * dx + (cy - y1) * dy) / len2

    // Find the closest point on the line from the circle
    val closest = Point(
        x1 + dot * dx,
        y1 + dot * dy
    )

    // Check that the closest point is on the line segment and inside the circle
    return isPointOnLine(closest, Line(x1, y1, x2, y2)) && isPointInsideCircle(closest, center, radius)
}

/**
 * Checks if a point defined by (x1,y1) is inside or on the circle defined by the center point (cx,cy) and radius, r.
 *
 * The algorithm used in this method is based on the Pythagorean Theorem:<br></br>
 * http://www.jeffreythompson.org/collision-detection/point-circle.php
 *
 * @param point is the point.
 * @param center is center point of the circle.
 * @param radius is the radius of the circle.
 * @return `true` if the point is inside or on the circle; `false` otherwise.
 */
fun isPointInsideCircle(point: Point, center: IPoint, radius: Double): Boolean {
    val dx = point.x - center.x
    val dy = point.y - center.y

    // If the distance is less or equal than the circle's radius the point is considered to be inside the circle
    return dx * dx + dy * dy <= radius * radius
}

/**
 * Checks if the point defined by (px,py) is on the line segment defined by the two points (x1,y1) and (x2,y2).
 *
 * The algorithm first checks if the "cross-product" between the vectors (x1,y1) -> (px,py) and (x1,y1) -> (x2,y2)
 * is equal to 0. If true, the point lies on the line. Secondly, the algorithm makes sure that the point is points
 * of the line segment.
 *
 * The algorithm is described by AnT here:<br>
 * <br>
 * https://stackoverflow.com/questions/11907947/how-to-check-if-a-point-lies-on-a-line-between-2-other-points/11908158#11908158
 *
 * @param point is the point.
 * @param line is the line segment.
 * @return `true` if the point is on the line segment; `false` otherwise.
 */
fun isPointOnLine(point: Point, line: Line): Boolean {
    val px = point.x
    val py = point.y

    val x1 = line.start.x
    val y1 = line.start.y
    val x2 = line.end.x
    val y2 = line.end.y

    // Calculate cross product of vectors
    val dxp = px - x1
    val dyp = py - y1
    val dxl = x2 - x1
    val dyl = y2 - y1
    val cross = dxp * dyl - dyp * dxl

    // Point lies on the line, if and only if, cross-product is equal to zero.
    if (!cross.isNearTo(0.0)) {
        return false
    }

    // Check whether it lies between the original points
    return if (abs(dxl) >= abs(dyl))
        if (dxl > 0) px in x1..x2 else px in x2..x1
    else
        if (dyl > 0) py in y1..y2 else py in y2..y1
}

/**
 * Checks if two line segments (a and b) are intersecting. Line segment a is defined by the two points a1 and a2,
 * and line segment b is defined by the two points b1 and b2.
 *
 * The algorithm used in this method is based on CommanderKeith's algorithm:<br></br>
 * http://www.java-gaming.org/index.php?topic=22590.0 <br></br>
 * http://gigglingcorpse.com/2015/06/25/line-segment-intersection/
 *
 * This algorithm is based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems
 * III". Keith Woodward added new code to optimize Franklin's original code.
 *
 * @param line1 is the 1st line segment,
 * @param line2 is the 2nd line segment,
 * @return `true` if the two lines are intersecting; `false` otherwise.
 */
fun isLineIntersectingLine(line1: Line, line2: Line): Boolean {
    val a1 = line1.start
    val a2 = line1.end
    val b1 = line2.start
    val b2 = line2.end

    // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III"
    // book (http://www.graphicsgems.org/)
    val ax = a2.x - a1.x
    val ay = a2.y - a1.y
    val bx = b1.x - b2.x
    val by = b1.y - b2.y
    val cx = a1.x - b1.x
    val cy = a1.y - b1.y
    val alphaNumerator = by * cx - bx * cy
    val commonDenominator = ay * bx - ax * by
    if (commonDenominator > 0 && (alphaNumerator < 0 || alphaNumerator > commonDenominator)) {
        return false
    } else if (commonDenominator < 0 && (alphaNumerator > 0 || alphaNumerator < commonDenominator)) {
        return false
    }
    val betaNumerator = ax * cy - ay * cx
    if (commonDenominator > 0 && (betaNumerator < 0 || betaNumerator > commonDenominator)) {
        return false
    } else if (commonDenominator < 0 && (betaNumerator > 0 || betaNumerator < commonDenominator)) {
        return false
    }
    if (commonDenominator == 0.0) {
        // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
        // The lines are parallel.
        // Check if they're collinear.
        val y3LessY1 = b1.y - a1.y
        // see http://mathworld.wolfram.com/Collinear.html
        val collinearityTestForP3 = a1.x * (a2.y - b1.y) + a2.x * y3LessY1 + b1.x * (a1.y - a2.y)
        // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
        if (collinearityTestForP3 == 0.0) {
            // The lines are collinear. Now check if they overlap.
            if (a1.x >= b1.x && a1.x <= b2.x || a1.x <= b1.x && a1.x >= b2.x || a2.x >= b1.x && a2.x <= b2.x || a2.x <= b1.x && a2.x >= b2.x || b1.x >= a1.x && b1.x <= a2.x || b1.x <= a1.x && b1.x >= a2.x) {
                return a1.y >= b1.y && a1.y <= b2.y || a1.y <= b1.y && a1.y >= b2.y || a2.y >= b1.y && a2.y <= b2.y || a2.y <= b1.y && a2.y >= b2.y || b1.y >= a1.y && b1.y <= a2.y || b1.y <= a1.y && b1.y >= a2.y
            }
        }
        return false
    }
    return true
}

/**
 * Checks if a circle is intersecting/inside a circle sector. The circle sector is defined by a sector center,
 * sector radius, a arc start angle, and arc end angle. The arc end angle must be greater that the arc start angle.
 *
 * The algorithm used in this method is based on Oren Trutner algorithm:
 * http://stackoverflow.com/questions/13652518/efficiently-find-points-inside-a-circle-sector
 *
 * @param circleCenter is center point of the circle.
 * @param circleRadius is the radius of the circle.
 * @param sectorCenter is the center point of the circle sector.
 * @param sectorRadius is the radius of the circle sector.
 * @param arcStartAngle is the arc start angle in degrees.
 * @param arcEndAngle is the arc end angle in degrees.
 * @return `true` if the circle lines is intersecting/inside the circle segment; `false` otherwise.
 */
fun isCircleIntersectingCircleSector(
    circleCenter: IPoint, circleRadius: Double,
    sectorCenter: IPoint, sectorRadius: Double,
    arcStartAngle: Double, arcEndAngle: Double
): Boolean {
    assert(arcEndAngle > arcStartAngle)
    val maxRadiusToPoint = sectorRadius + circleRadius
    val vx = circleCenter.x - sectorCenter.x
    val vy = circleCenter.y - sectorCenter.y

    // Check if point is outside max radius to point
    if (vx * vx + vy * vy > maxRadiusToPoint * maxRadiusToPoint) {
        return false // outside radius
    }
    val arcStartRad = Math.toRadians(arcStartAngle)
    val arcEndRad = Math.toRadians(arcEndAngle)
    var dx = cos(arcStartRad) * sectorRadius
    var dy = sin(arcStartRad) * sectorRadius
    val (x, y) = Point(dx, dy)
    dx = cos(arcEndRad) * sectorRadius
    dy = sin(arcEndRad) * sectorRadius
    val (x1, y1) = Point(dx, dy)

    // Check if circle center is within the circle sector arms
    return if (!isClockwise(x, y, vx, vy) && isClockwise(x1, y1, vx, vy)) {
        true
    } else {
        // Check if circle is intersecting one of the arms
        isLineIntersectingCircle(
            Line(sectorCenter.x, sectorCenter.y, sectorCenter.x + x, sectorCenter.y + y),
            circleCenter,
            circleRadius
        ) || isLineIntersectingCircle(
            Line(sectorCenter.x, sectorCenter.y, sectorCenter.x + x1, sectorCenter.y + y1),
            circleCenter,
            circleRadius
        )
    }
}

/**
 * Checks if vector v1 is clockwise to vector v2 compared to a shared starting point.
 * @param v1x is the x coordinate of vector v1.
 * @param v1y is the y coordinate of vector v1.
 * @param v2x is the x coordinate of vector v2.
 * @param v2y is the y coordinate of vector v2.
 * @return `true` if v1 is clockwise to v2; `false` otherwise.
 */
fun isClockwise(v1x: Double, v1y: Double, v2x: Double, v2y: Double): Boolean = v1x * v2y <= v1y * v2x

/**
 * Returns a random direction in the range 0 up to 360 degrees.
 * @return direction in degrees in the range [0;360[ degrees.
 */
fun randomDirection(): Double = Math.random() * 360

/**
 * Calculates the direction in degrees to point (targetX, targetY) from point (sourceX, sourceY).
 *
 * @param sourceX is the x coordinate of the source point.
 * @param sourceY is the y coordinate of the source point.
 * @param targetX is the x coordinate of the target point.
 * @param targetY is the y coordinate of the target point.
 * @return the angle from the source point to the destination point measured in degrees.
 */
fun angle(sourceX: Double, sourceY: Double, targetX: Double, targetY: Double): Double =
    normalizeAbsoluteDegrees(Math.toDegrees(atan2(targetY - sourceY, targetX - sourceX)))