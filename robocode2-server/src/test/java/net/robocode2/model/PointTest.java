package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PointTest {

	@Test
	public void constructorParams() {
		Point point = new Point(12.34, 45.67);

		assertEquals(12.34, point.x, 0.0001);
		assertEquals(45.67, point.y, 0.0001);

		point = new Point(-76.45, -89.12);

		assertEquals(-76.45, point.x, 0.0001);
		assertEquals(-89.12, point.y, 0.0001);
	}
}
