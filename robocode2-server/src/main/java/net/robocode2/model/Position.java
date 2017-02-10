package net.robocode2.model;

public final class Position {

	public final double x;
	public final double y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("(%f %f)", x, y);
	}
}