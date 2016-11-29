package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.Bullet;
import net.robocode2.model.Position;

public final class BulletToBulletStateMapper {

	public static BulletState map(Bullet bullet) {
		BulletState bulletState = new BulletState();

		Position startPosition = bullet.getFirePosition();
		int tick = bullet.getTick();
		double direction = bullet.getDirection();
		double speed = bullet.getSpeed();

		bulletState.setBotId(bullet.getBotId());
		bulletState.setBulletId(bullet.getBulletId());
		bulletState.setDirection(direction);
		bulletState.setPower(bullet.getPower());
		bulletState.setSpeed(speed);

		double x = startPosition.getX() + Math.cos(direction) * speed * tick;
		double y = startPosition.getY() + Math.sin(direction) * speed * tick;

		net.robocode2.json_schema.types.Position position = new net.robocode2.json_schema.types.Position();
		position.setX(x);
		position.setY(y);

		bulletState.setPosition(position);

		return bulletState;
	}
}
