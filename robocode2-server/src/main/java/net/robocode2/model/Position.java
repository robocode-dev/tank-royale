package net.robocode2.model;

public final class Position {

	public final double x;
	public final double y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// Direction in degrees
	public Position calcNewPosition(double direction, double speed) {
		double angle = Math.toRadians(direction);
		return new Position(x + Math.cos(angle) * speed, y + Math.sin(angle) * speed);
	}
}