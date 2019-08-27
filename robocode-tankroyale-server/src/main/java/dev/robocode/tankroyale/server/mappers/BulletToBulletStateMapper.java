package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.Bullet;
import dev.robocode.tankroyale.server.model.Point;
import dev.robocode.tankroyale.server.util.MathUtil;
import dev.robocode.tankroyale.schema.BulletState;

@SuppressWarnings("WeakerAccess")
public final class BulletToBulletStateMapper {

	private BulletToBulletStateMapper() {}

	public static BulletState map(Bullet bullet) {
		BulletState bulletState = new BulletState();

		double direction = bullet.getDirection();
		double speed = bullet.getSpeed();

		bulletState.setOwnerId(bullet.getBotId());
		bulletState.setBulletId(bullet.getBulletId());
		bulletState.setDirection(MathUtil.normalAbsoluteDegrees(direction));
		bulletState.setPower(bullet.getPower());
		bulletState.setSpeed(speed);
		Point pos = bullet.calcPosition();
		bulletState.setX(pos.x);
		bulletState.setY(pos.y);

		return bulletState;
	}
}
