package dev.robocode.tankroyale.server.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SizeTest {

	@Test
	public void constructorParams() {
		Size size = new Size(12.34, 45.67);

		assertEquals(12.34, size.width, 0.0001);
		assertEquals(45.67, size.height, 0.0001);

		size = new Size(-76.45, -89.12);

		assertEquals(-76.45, size.width, 0.0001);
		assertEquals(-89.12, size.height, 0.0001);
	}
}
