package net.robocode2.model;

public interface IBullet {

	int getBotId();

	int getBulletId();

	double getPower();

	Point getFirePosition();

	double getDirection();

	double getSpeed();

	int getTick();

	public static Point calcPosition(Point firePosition, double direction, double speed, int tick) {
		double angle = Math.toRadians(direction);
		double distance = speed * tick;
		double x = firePosition.x + Math.cos(angle) * distance;
		double y = firePosition.y + Math.sin(angle) * distance;
		return new Point(x, y);
	}

	/**
	 * Calculates the current bullet position based on the fire position and current tick.
	 */
	public default Point calcPosition() {
		return calcPosition(getFirePosition(), getDirection(), getSpeed(), getTick());
	}

	public default Point calcNextPosition() {
		return calcPosition(getFirePosition(), getDirection(), getSpeed(), getTick() + 1);
	}
}