package net.robocode2.model;

public final class ScanArc {

	private final double angle;
	private final double length;

	public ScanArc(double angle, double length) {
		this.angle = angle;
		this.length = length;
	}

	public double getAngle() {
		return angle;
	}

	public double getLength() {
		return length;
	}
}