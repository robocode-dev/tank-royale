package net.robocode2.model;

public final class ScanField {

	private final double angle;
	private final double radius;

	public ScanField(double angle, double radius) {
		this.angle = angle;
		this.radius = radius;
	}

	public double getAngle() {
		return angle;
	}

	public double getRadius() {
		return radius;
	}
}