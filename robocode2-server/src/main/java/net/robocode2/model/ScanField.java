package net.robocode2.model;

/**
 * Defines the scanning field of a bot
 * 
 * @author Flemming N. Larsen
 */
public final class ScanField {

	/** Spread angle */
	private final double angle;
	/** Scanning radius */
	private final double radius;

	/**
	 * Creates a new scanning field
	 * 
	 * @param angle
	 *            is the spread angle of the scanning field
	 * @param radius
	 *            is the radius of the scanning field
	 */
	public ScanField(double angle, double radius) {
		this.angle = angle;
		this.radius = radius;
	}

	/** Returns the spread angle */
	public double getAngle() {
		return angle;
	}

	/** Returns the scan radius */
	public double getRadius() {
		return radius;
	}
}