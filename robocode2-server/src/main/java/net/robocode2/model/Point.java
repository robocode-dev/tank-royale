package net.robocode2.model;

public final class Point {

	public final double x;
	public final double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("(%f %f)", x, y);
	}
}