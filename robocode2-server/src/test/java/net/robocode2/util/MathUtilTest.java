package net.robocode2.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathUtilTest {

	@Test
	public void normalAbsoluteDegrees() {
		// In normal range [0,360[
		assertEquals(0, MathUtil.normalAbsoluteDegrees(0), 1E-8);
		assertEquals(359.999999, MathUtil.normalAbsoluteDegrees(359.999999), 1E-8);
		assertEquals(124.57, MathUtil.normalAbsoluteDegrees(124.57), 1E-8);
		assertEquals(256.48, MathUtil.normalAbsoluteDegrees(256.48), 1E-8);

		// Very close to the normalized range
		assertEquals(0, MathUtil.normalAbsoluteDegrees(360), 1E-8);
		assertEquals(359.999999, MathUtil.normalAbsoluteDegrees(-0.000001), 1E-8);

		// Normalized angles
		assertEquals(360 - 200, MathUtil.normalAbsoluteDegrees(-200), 1E-8);
		assertEquals(500 - 360, MathUtil.normalAbsoluteDegrees(500), 1E-8);

		// Far from the normalized range
		assertEquals(0, MathUtil.normalAbsoluteDegrees(360 * 100), 1E-8);
		assertEquals(180, MathUtil.normalAbsoluteDegrees(-180 * 101), 1E-8);
	}

	@Test
	public void normalRelativeDegrees() {
		// In normal range [-180,180[
		assertEquals(-180, MathUtil.normalRelativeDegrees(-180), 1E-8);
		assertEquals(179.999999, MathUtil.normalRelativeDegrees(179.999999), 1E-8);
		assertEquals(124.57, MathUtil.normalRelativeDegrees(124.57), 1E-8);
		assertEquals(-56.48, MathUtil.normalRelativeDegrees(-56.48), 1E-8);

		// Very close to the normalized range
		assertEquals(-180, MathUtil.normalRelativeDegrees(180), 1E-8);
		assertEquals(179.999999, MathUtil.normalRelativeDegrees(-180.000001), 1E-8);

		// Normalized angles
		assertEquals(360 - 200, MathUtil.normalRelativeDegrees(-200), 1E-8);
		assertEquals(500 - 360, MathUtil.normalRelativeDegrees(500), 1E-8);

		// Far from the normalized range
		assertEquals(0, MathUtil.normalRelativeDegrees(360 * 100), 1E-8);
		assertEquals(-180, MathUtil.normalRelativeDegrees(-180 * 101), 1E-8);
	}
}
