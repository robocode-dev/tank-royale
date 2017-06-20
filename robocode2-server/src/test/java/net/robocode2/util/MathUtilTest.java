package net.robocode2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.robocode2.model.Point;

public class MathUtilTest {

	@Test
	public void normalAbsoluteDegrees() {
		// In normal range [0,360[
		assertEquals(0, MathUtil.normalAbsoluteDegrees(0), 1E-6);
		assertEquals(359.999999, MathUtil.normalAbsoluteDegrees(359.999999), 1E-6);
		assertEquals(124.57, MathUtil.normalAbsoluteDegrees(124.57), 1E-6);
		assertEquals(256.48, MathUtil.normalAbsoluteDegrees(256.48), 1E-6);

		// Very close to the normalized range
		assertEquals(0, MathUtil.normalAbsoluteDegrees(360), 1E-6);
		assertEquals(359.999999, MathUtil.normalAbsoluteDegrees(-0.000001), 1E-6);

		// Normalized angles
		assertEquals(360 - 200, MathUtil.normalAbsoluteDegrees(-200), 1E-6);
		assertEquals(500 - 360, MathUtil.normalAbsoluteDegrees(500), 1E-6);

		// Far from the normalized range
		assertEquals(0, MathUtil.normalAbsoluteDegrees(360 * 100), 1E-6);
		assertEquals(180, MathUtil.normalAbsoluteDegrees(-180 * 101), 1E-6);
	}

	@Test
	public void normalRelativeDegrees() {
		// In normal range [-180,180[
		assertEquals(-180, MathUtil.normalRelativeDegrees(-180), 1E-6);
		assertEquals(179.999999, MathUtil.normalRelativeDegrees(179.999999), 1E-6);
		assertEquals(124.57, MathUtil.normalRelativeDegrees(124.57), 1E-6);
		assertEquals(-56.48, MathUtil.normalRelativeDegrees(-56.48), 1E-6);

		// Very close to the normalized range
		assertEquals(-180, MathUtil.normalRelativeDegrees(180), 1E-6);
		assertEquals(179.999999, MathUtil.normalRelativeDegrees(-180.000001), 1E-6);

		// Normalized angles
		assertEquals(360 - 200, MathUtil.normalRelativeDegrees(-200), 1E-6);
		assertEquals(500 - 360, MathUtil.normalRelativeDegrees(500), 1E-6);

		// Far from the normalized range
		assertEquals(0, MathUtil.normalRelativeDegrees(360 * 100), 1E-6);
		assertEquals(-180, MathUtil.normalRelativeDegrees(-180 * 101), 1E-6);
	}

	@Test
	public void nearlyEquals() {
		assertTrue(MathUtil.nearlyEqual(0.0, 0.0000009999));
		assertFalse(MathUtil.nearlyEqual(0.0, 0.000001));

		assertFalse(MathUtil.nearlyEqual(-0.0, -0.000001));
		assertTrue(MathUtil.nearlyEqual(-0.0, -0.0000009999));

		assertTrue(MathUtil.nearlyEqual(1.1, 1.1000009999));
		assertTrue(MathUtil.nearlyEqual(1.1, 1.100001));
		assertFalse(MathUtil.nearlyEqual(1.1, 1.1000011));
	}

	@Test
	public void distance() {
		Point p0 = new Point(0, 0);
		Point p1 = new Point(1.234, 0.5);
		Point p2 = new Point(10.789, -2.345);
		Point p3 = new Point(-3.141, 5.999);
		Point p4 = new Point(-2.519, -7.886);

		assertEquals(0, MathUtil.distance(p0, p0), 1E-6);
		assertEquals(0, MathUtil.distance(p1, p1), 1E-6);
		assertEquals(0, MathUtil.distance(p2, p2), 1E-6);
		assertEquals(0, MathUtil.distance(p3, p3), 1E-6);
		assertEquals(0, MathUtil.distance(p4, p4), 1E-6);

		assertEquals(Math.hypot(p1.x - p2.x, p1.y - p2.y), MathUtil.distance(p1, p2), 1E-6);
		assertEquals(Math.hypot(p1.x - p3.x, p1.y - p3.y), MathUtil.distance(p1, p3), 1E-6);
		assertEquals(Math.hypot(p1.x - p4.x, p1.y - p4.y), MathUtil.distance(p1, p4), 1E-6);

		assertEquals(Math.hypot(p2.x - p1.x, p2.y - p1.y), MathUtil.distance(p2, p1), 1E-6);
		assertEquals(Math.hypot(p2.x - p3.x, p2.y - p3.y), MathUtil.distance(p2, p3), 1E-6);
		assertEquals(Math.hypot(p2.x - p4.x, p2.y - p4.y), MathUtil.distance(p2, p4), 1E-6);

		assertEquals(Math.hypot(p3.x - p1.x, p3.y - p1.y), MathUtil.distance(p3, p1), 1E-6);
		assertEquals(Math.hypot(p3.x - p2.x, p3.y - p2.y), MathUtil.distance(p3, p2), 1E-6);
		assertEquals(Math.hypot(p3.x - p4.x, p3.y - p4.y), MathUtil.distance(p3, p4), 1E-6);

		assertEquals(Math.hypot(p4.x - p1.x, p4.y - p1.y), MathUtil.distance(p4, p1), 1E-6);
		assertEquals(Math.hypot(p4.x - p2.x, p4.y - p2.y), MathUtil.distance(p4, p2), 1E-6);
		assertEquals(Math.hypot(p4.x - p3.x, p4.y - p3.y), MathUtil.distance(p4, p3), 1E-6);
	}

	@Test
	public void isLineIntersectingCircle() {
		// line(0,0)-(0,0), circle(0,0),r=0
		assertTrue(MathUtil.isLineIntersectingCircle(0, 0, 0, 0, 0, 0, 0));

		//// Lines are tangents on the circle

		// line(2,0)-(0,0), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(2, 0, 0, 0, 1, 1, 1));

		// line(0,2)-(0,0), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(0, 2, 0, 0, 1, 1, 1));

		// line(0,0)-(2,0), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(0, 0, 2, 0, 1, 1, 1));

		// line(0,0)-(0,2), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(0, 0, 0, 2, 1, 1, 1));

		// line(0,1)-(1,2), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(0, 1, 1, 2, 1, 1, 1));

		// line(1,2)-(2,1), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(1, 2, 2, 1, 1, 1, 1));

		// line(2,1)-(1,0), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(2, 1, 1, 0, 1, 1, 1));

		// line(0,1)-(1,0), circle(1,1),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(0, 1, 1, 0, 1, 1, 1));

		//// Lines are near to but not tangents on the circle

		// line(2,0)-(0,0), circle(1,1),r=0.999999
		assertFalse(MathUtil.isLineIntersectingCircle(2, 0, 0, 0, 1, 1, 0.999999));

		// line(0,2)-(0,0), circle(1,1),r=0.999999
		assertFalse(MathUtil.isLineIntersectingCircle(0, 2, 0, 0, 1, 1, 0.999999));

		// line(0,0)-(2,0), circle(1,1),r=0.999999
		assertFalse(MathUtil.isLineIntersectingCircle(0, 0, 2, 0, 1, 1, 0.999999));

		// line(0,0)-(0,2), circle(1,1),r=0.999999
		assertFalse(MathUtil.isLineIntersectingCircle(0, 0, 0, 2, 1, 1, 0.999999));

		// line(0,1)-(1,2), circle(1,1),r=0.999999
		assertTrue(MathUtil.isLineIntersectingCircle(0, 1, 1, 2, 1, 1, 0.999999));

		// line(1,2)-(2,1), circle(1,1),r=0.999999
		assertTrue(MathUtil.isLineIntersectingCircle(1, 2, 2, 1, 1, 1, 0.999999));

		// line(2,1)-(1,0), circle(1,1),r=0.999999
		assertTrue(MathUtil.isLineIntersectingCircle(2, 1, 1, 0, 1, 1, 0.999999));

		// line(0,1)-(1,0), circle(1,1),r=0.999999
		assertTrue(MathUtil.isLineIntersectingCircle(0, 1, 1, 0, 1, 1, 0.999999));

		//// Whole line is inside circle

		// line(-1,-1)-(1,1), circle(0,0),r=2
		assertTrue(MathUtil.isLineIntersectingCircle(-1, -1, 1, 1, 0, 0, 2));

		// line(1,1)-(2,2), circle(3,3),r=2
		assertTrue(MathUtil.isLineIntersectingCircle(1, 1, 2, 2, 3, 3, 2));

		//// Line is partly inside, but also outside the circle

		// line(0,0.5)-(0,1.5), circle(0,0),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(0, 0.5, 0, 1.5, 0, 0, 1));

		//// Line is going thru the circle from one side to the other

		// line(1,1)-(10,10), circle(3,3),r=0.01
		assertTrue(MathUtil.isLineIntersectingCircle(1, 1, 10, 10, 3, 3, 0.01));

		// line(1,1)-(10,10), circle(9,9),r=0.00001
		assertTrue(MathUtil.isLineIntersectingCircle(1, 1, 10, 10, 9, 9, 0.00001));

		//// Line end just touching the circle

		// line(1,0)-(2,0), circle(0,0),r=1
		assertTrue(MathUtil.isLineIntersectingCircle(1, 0, 2, 0, 0, 0, 1));

		// line(1.00001,0)-(2,0), circle(0,0),r=1
		assertFalse(MathUtil.isLineIntersectingCircle(1.0001, 0, 2, 0, 0, 0, 1));
	}

	@Test
	public void isPointInsideCircle() {

		//// Inside the circle

		// point(0,0), circle(0,0),r=0
		assertTrue(MathUtil.isPointInsideCircle(0, 0, 0, 0, 0));

		// point(-1,-1), circle(-1,-1),r=1
		assertTrue(MathUtil.isPointInsideCircle(-1, -1, -1, -1, 1));

		// point(-1.5,-1), circle(-1,-1),r=1
		assertTrue(MathUtil.isPointInsideCircle(-1.5, -1, -1, -1, 1));

		// point(-1,-1.5), circle(-1,-1),r=1
		assertTrue(MathUtil.isPointInsideCircle(-1, -1.5, -1, -1, 1));

		// point(-1,-1.5), circle(-1,-1),r=1
		assertTrue(MathUtil.isPointInsideCircle(-1, -1.5, -1, -1, 1));

		// point(1,1), circle(-1,-1),r=3
		assertTrue(MathUtil.isPointInsideCircle(1, 1, -1, -1, 3));

		//// Not touching the circle at all

		// point(0.25,0.25), circle(1,1),r=1
		assertFalse(MathUtil.isPointInsideCircle(0.25, 0.25, 1, 1, 1));

		// point(0.25,1.75), circle(1,1),r=1
		assertFalse(MathUtil.isPointInsideCircle(0.25, 1.75, 1, 1, 1));

		// point(1.75,1.75), circle(1,1),r=1
		assertFalse(MathUtil.isPointInsideCircle(1.75, 1.75, 1, 1, 1));

		// point(1.75,0.25), circle(1,1),r=1
		assertFalse(MathUtil.isPointInsideCircle(1.75, 0.25, 1, 1, 1));

		//// Touching the circle

		// point(0,1), circle(1,1),r=1
		assertTrue(MathUtil.isPointInsideCircle(0, 1, 1, 1, 1));

		// point(1,2), circle(1,1),r=1
		assertTrue(MathUtil.isPointInsideCircle(1, 2, 1, 1, 1));

		// point(2,1), circle(1,1),r=1
		assertTrue(MathUtil.isPointInsideCircle(2, 1, 1, 1, 1));

		// point(1,0), circle(1,1),r=1
		assertTrue(MathUtil.isPointInsideCircle(1, 0, 1, 1, 1));

		//// Almost touching the circle

		// point(0,1), circle(1,1),r=0.99999
		assertFalse(MathUtil.isPointInsideCircle(0, 1, 1, 1, 0.99999));

		// point(1,2), circle(1,1),r=0.99999
		assertFalse(MathUtil.isPointInsideCircle(1, 2, 1, 1, 0.99999));

		// point(2,1), circle(1,1),r=0.99999
		assertFalse(MathUtil.isPointInsideCircle(2, 1, 1, 1, 0.99999));

		// point(1,0), circle(1,1),r=0.99999
		assertFalse(MathUtil.isPointInsideCircle(1, 0, 1, 1, 0.99999));
	}

	@Test
	public void isPointOnLine() {
		// point(0,0), line(0,0)-(0,0)
		assertTrue(MathUtil.isPointOnLine(0, 0, 0, 0, 0, 0));

		//// Point at the end of the line segment

		// point(2,1), line(2,1)-(10,5)
		assertTrue(MathUtil.isPointOnLine(2, 1, 2, 1, 10, 5));

		// point(10,5), line(2,1)-(10,5)
		assertTrue(MathUtil.isPointOnLine(10, 5, 2, 1, 10, 5));

		// point(2,1), line(2.0001,1)-(10,5)
		assertFalse(MathUtil.isPointOnLine(2.001, 1, 2, 1, 10, 5));

		// point(10,5.000001), line(2,1)-(10,5)
		assertFalse(MathUtil.isPointOnLine(10, 5.000001, 2, 1, 10, 5));

		//// Negative values

		// point(-2,-1), line(-2,-1)-(-10,-5)
		assertTrue(MathUtil.isPointOnLine(-2, -1, -2, -1, -10, -5));

		// point(-10,-5), line(-2,-1)-(-10,-5)
		assertTrue(MathUtil.isPointOnLine(-10, -5, -2, -1, -10, -5));

		//// Point on the line segment

		// point(4,2), line(2,1)-(10,5)
		assertTrue(MathUtil.isPointOnLine(4, 2, 2, 1, 10, 5));

		// point(6,3), line(2,1)-(10,5)
		assertTrue(MathUtil.isPointOnLine(6, 3, 2, 1, 10, 5));

		// point(4.0001,2), line(2,1)-(10,5)
		assertFalse(MathUtil.isPointOnLine(4.0001, 2, 2, 1, 10, 5));

		// point(6,2.9999), line(2,1)-(10,5)
		assertFalse(MathUtil.isPointOnLine(6, 3, 2.9999, 1, 10, 5));
	}

	@Test
	public void isLineIntersectingLine() {
		// Crossing

		assertTrue(MathUtil.isLineIntersectingLine(new Point(0, 10), new Point(0, -10), new Point(-10, 0),
				new Point(0, 10)));
		assertTrue(MathUtil.isLineIntersectingLine(new Point(0, 0), new Point(10, 10), new Point(0, 10),
				new Point(10, 0)));
		assertTrue(MathUtil.isLineIntersectingLine(new Point(10, 5), new Point(100, 20), new Point(10, 6),
				new Point(100, 10)));

		// Parallel lines, not touching. But very close to each other

		assertFalse(MathUtil.isLineIntersectingLine(new Point(10, 5), new Point(100, 20), new Point(10, 5.000001),
				new Point(100, 20.000001)));

		// Two individual lines (different directions), sharing and end-point

		assertTrue(MathUtil.isLineIntersectingLine(new Point(5, 7), new Point(30, 20), new Point(5, 7),
				new Point(-10, -20)));
		assertFalse(MathUtil.isLineIntersectingLine(new Point(4.99999999, 7), new Point(30, 20), new Point(5, 7),
				new Point(-10, -20)));

		// End-point on one line segment touching the another line segment, but no crossing

		assertTrue(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(2, 3), new Point(2, 3), new Point(3, 5)));
		assertFalse(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(2, 2.999999999), new Point(2, 3),
				new Point(3, 5)));

		// Almost Parallel lines very very close with same end point

		assertTrue(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(100000, 1.0000001), new Point(1, 1),
				new Point(100000, 1.00000011)));
		assertFalse(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(100000, 1.0000001),
				new Point(1, 1.0000001), new Point(100000, 1.00000011)));

		// Two identical line segments overlapping each other

		assertTrue(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(100000, 1.0000001), new Point(1, 1),
				new Point(100000, 1.0000001)));

		// Line segment inside another line segment, but with no shared end-points

		assertTrue(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(2, 3), new Point(3, 5)));

		// Line segment within another line segment with a shared end-point

		assertTrue(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(1, 1), new Point(3, 5)));
		assertTrue(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(3, 5), new Point(5, 9)));

		// Two line segments on the same "line" sharing a line segment in between (overlapping)
		assertTrue(
				MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(3, 5), new Point(7, 13)));
		assertFalse(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(3, 5.00001),
				new Point(7, 13)));

		// One line segment with an end-point just touching another line segment
		assertTrue(
				MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(3, 5), new Point(10, 10)));
		assertFalse(MathUtil.isLineIntersectingLine(new Point(1, 1), new Point(5, 9), new Point(3.0000001, 4.999999),
				new Point(10, 10)));
	}

	@Test
	public void isCircleIntersectingCircleSector() {
		// circle center(cx,cy),cr=x, sector center(sx,sy),sr=x, arcStart=x, arcEnd=x

		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(0, 0), 10, 90, 0));

		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, 10), 10, 90, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, 10), 10, 90, 360));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, 10), 10, 0, 90));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, 10), 10, 360, 90));

		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(-10, 10), 10, 180, 90));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(-10, 10), 10, 90, 180));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(-10, -10), 10, 180, 270));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(-10, -10), 10, 270, 180));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, -10), 10, 270, 360));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, -10), 10, 270, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, -10), 10, 0, 270));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(0, 0), 5, new Point(10, -10), 10, 360, 270));

		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(10, 10), 4, new Point(0, 0), 10, 0, 359.999));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(10, -10), 4, new Point(0, 0), 10, 0, 359.999));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(-10, 10), 4, new Point(0, 0), 10, 0, 359.999));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(-10, -10), 4, new Point(0, 0), 10, 0, 359.999));

		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(10, 10), 4, new Point(0, 0), 10, 359.999, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(10, -10), 4, new Point(0, 0), 10, 359.999, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(-10, 10), 4, new Point(0, 0), 10, 359.999, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(-10, -10), 4, new Point(0, 0), 10, 359.999, 0));

		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(10, 10), 5, new Point(0, 0), 10, 90, 0));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(10, -10), 10, new Point(0, 0), 10, 0, 359.999));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(-10, 10), 10, new Point(0, 0), 10, 180, 179.9));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(-10, -10), 10, new Point(0, 0), 10, 270, 269.9));

		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(-5, -5), 7.0711, new Point(0, 0), 10, 90, 0));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(5, -5), 5, new Point(0, 0), 10, 90, 0));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(-5, 5), 5, new Point(0, 0), 10, 90, 0));

		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(-5, 15), 7.0711, new Point(0, 0), 10, 90, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(-5, 15), 7.071, new Point(0, 0), 10, 90, 0));
		assertTrue(MathUtil.isCircleIntersectingCircleSector(new Point(15, -5), 7.0711, new Point(0, 0), 10, 90, 0));
		assertFalse(MathUtil.isCircleIntersectingCircleSector(new Point(15, -5), 7.071, new Point(0, 0), 10, 90, 0));
	}

	@Test
	public void isClockwise() {
		// 90 deg => 0 deg is clockwise
		assertTrue(MathUtil.isClockwise(0, 10, 10, 0));
		// 0 deg => 90 deg is anti-clockwise
		assertFalse(MathUtil.isClockwise(10, 0, 0, 10));

		assertTrue(MathUtil.isClockwise(-10, 2, -5, 7));
		assertFalse(MathUtil.isClockwise(-5, 7, -10, 2));

		assertTrue(MathUtil.isClockwise(-5, -7, -10, -2));
		assertFalse(MathUtil.isClockwise(-10, -2, -5, -7));

		assertTrue(MathUtil.isClockwise(10, 0.0000001, 10, 0));
		assertTrue(MathUtil.isClockwise(10, 0, 10, -0.0000001));
	}
}
