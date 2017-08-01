package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ScanFieldTest {

	@Test
	public void constructorParams() {
		ScanField scanField = new ScanField(12.34, 56.78);

		assertEquals(12.34, scanField.getAngle(), 0.00001);
		assertEquals(56.78, scanField.getRadius(), 0.00001);
	}
}
