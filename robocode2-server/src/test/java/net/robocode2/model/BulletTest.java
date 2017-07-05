package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class BulletTest {

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
	public void constructorEmpty() {
		Bullet bullet = new Bullet();
		assertEquals(0, bullet.getBotId());
		assertEquals(0, bullet.getBulletId());
		assertEquals(0, bullet.getPower(), 0.00001);
		assertNull(bullet.getFirePosition());
		assertEquals(0, bullet.getDirection(), 0.00001);
		assertEquals(0, bullet.getSpeed(), 0.00001);
		assertEquals(0, bullet.getTick());
	}

	@Test
	public void constructorIBullet() {
		assertReflectionEquals(initializedBullet, new Bullet(initializedBullet));
	}

	@Test
	public void toImmutableBullet() {
		assertReflectionEquals(initializedBullet.toImmutableBullet(),
				new Bullet(initializedBullet).toImmutableBullet());
	}

	@Test
	public void setBotId() {
		Bullet bullet = new Bullet();
		bullet.setBotId(7913);
		assertEquals(7913, bullet.getBotId());
	}

	@Test
	public void setBulletId() {
		Bullet bullet = new Bullet();
		bullet.setBulletId(7913);
		assertEquals(7913, bullet.getBulletId());
	}

	@Test
	public void setPower() {
		Bullet bullet = new Bullet();
		bullet.setPower(7913);
		assertEquals(7913, bullet.getPower(), 0.0001);
	}

	@Test
	public void setFirePosition() {
		Bullet bullet = new Bullet();
		bullet.setFirePosition(new Point(12.34, 56.78));
		assertReflectionEquals(new Point(12.34, 56.78), bullet.getFirePosition());

		bullet.setFirePosition(null);
		assertNull(bullet.getFirePosition());
	}

	@Test
	public void setDirection() {
		Bullet bullet = new Bullet();
		bullet.setDirection(79.13);
		assertEquals(79.13, bullet.getDirection(), 0.0001);
	}

	@Test
	public void setSpeed() {
		Bullet bullet = new Bullet();
		bullet.setSpeed(79.13);
		assertEquals(79.13, bullet.getSpeed(), 0.0001);
	}

	@Test
	public void setTick() {
		Bullet bullet = new Bullet();
		bullet.setTick(7913);
		assertEquals(7913, bullet.getTick());
	}

	@Test
	public void incrementTick() {
		Bullet bullet = new Bullet();
		bullet.setTick(7913);
		bullet.incrementTick();
		assertEquals(7913 + 1, bullet.getTick());
	}
}
