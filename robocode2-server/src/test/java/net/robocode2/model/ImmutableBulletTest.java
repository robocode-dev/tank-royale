package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableBulletTest {

	static Bullet initializedBullet;

	@BeforeClass
	public static void initialize() {
		Bullet bullet = new Bullet();
		bullet.setBotId(1234);
		bullet.setBulletId(2345);
		bullet.setPower(1.234);
		bullet.setFirePosition(new Point(12.34, 45.67));
		bullet.setDirection(234.56);
		bullet.setSpeed(5.67);
		bullet.setTick(87);

		initializedBullet = bullet;
	}

	@Test
	public void constructorIBullet() {
		ImmutableBullet bullet = new ImmutableBullet(initializedBullet);

		assertEquals(initializedBullet.getOwnerId(), bullet.getOwnerId());
		assertEquals(initializedBullet.getBulletId(), bullet.getBulletId());
		assertEquals(initializedBullet.getPower(), bullet.getPower(), 0.00001);
		assertEquals(initializedBullet.getFirePosition(), bullet.getFirePosition());
		assertEquals(initializedBullet.getDirection(), bullet.getDirection(), 0.00001);
		assertEquals(initializedBullet.getSpeed(), bullet.getSpeed(), 0.00001);
		assertEquals(initializedBullet.getTick(), bullet.getTick());
	}
}
