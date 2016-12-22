package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.Bullet;

public final class BulletToBulletStateMapper {

	public static BulletState map(Bullet bullet) {
		BulletState bulletState = new BulletState();

		double direction = bullet.getDirection();
		double speed = bullet.getSpeed();

		bulletState.setBotId(bullet.getBotId());
		bulletState.setBulletId(bullet.getBulletId());
		bulletState.setDirection(direction);
		bulletState.setPower(bullet.getPower());
		bulletState.setSpeed(speed);
		bulletState.setPosition(PositionMapper.map(bullet.calcPosition()));

		return bulletState;
	}
}
