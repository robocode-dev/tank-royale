package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BulletState;
import net.robocode2.model.IBullet;
import net.robocode2.util.MathUtil;

public final class BulletToBulletStateMapper {

	public static BulletState map(IBullet bullet) {
		BulletState bulletState = new BulletState();

		double direction = bullet.getDirection();
		double speed = bullet.getSpeed();

		bulletState.setBotId(bullet.getBotId());
		bulletState.setBulletId(bullet.getBulletId());
		bulletState.setDirection(MathUtil.normalAbsoluteAngleDegrees(direction));
		bulletState.setPower(bullet.getPower());
		bulletState.setSpeed(speed);
		bulletState.setPosition(PointMapper.map(bullet.calcPosition()));

		return bulletState;
	}
}
