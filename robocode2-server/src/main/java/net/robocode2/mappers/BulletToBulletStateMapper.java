package net.robocode2.mappers;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.Bullet;
import net.robocode2.util.MathUtil;

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
		bulletState.setPosition(PointMapper.map(bullet.calcPosition()));

		return bulletState;
	}
}
