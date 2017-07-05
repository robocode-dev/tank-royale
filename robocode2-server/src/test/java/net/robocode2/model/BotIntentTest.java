package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class BotIntentTest {

	@Test
	public void constructorEmpty() {
		BotIntent intent = new BotIntent();

		assertNull(intent.getTargetSpeed());
		assertNull(intent.getDrivingTurnRate());
		assertNull(intent.getGunTurnRate());
		assertNull(intent.getRadarTurnRate());
		assertNull(intent.getBulletPower());
	}

	@Test
	public void update() {
		BotIntent intent = new BotIntent();

		assertNull(intent.getTargetSpeed());
		assertNull(intent.getDrivingTurnRate());
		assertNull(intent.getGunTurnRate());
		assertNull(intent.getRadarTurnRate());
		assertNull(intent.getBulletPower());

		double targetSpeed = 1.2;
		double drivingTurnRate = 2.3;
		double gunTurnRate = 3.4;
		double radarTurnRate = 4.5;
		double bulletPower = 5.6;

		intent.update(new ImmutableBotIntent(targetSpeed, null, gunTurnRate, null, bulletPower));

		assertEquals(targetSpeed, intent.getTargetSpeed(), 0.00001);
		assertNull(intent.getDrivingTurnRate());
		assertEquals(gunTurnRate, intent.getGunTurnRate(), 0.00001);
		assertNull(intent.getRadarTurnRate());
		assertEquals(bulletPower, intent.getBulletPower(), 0.00001);

		intent.update(new ImmutableBotIntent(null, drivingTurnRate, null, radarTurnRate, null));

		assertEquals(targetSpeed, intent.getTargetSpeed(), 0.00001);
		assertEquals(drivingTurnRate, intent.getDrivingTurnRate(), 0.00001);
		assertEquals(gunTurnRate, intent.getGunTurnRate(), 0.00001);
		assertEquals(radarTurnRate, intent.getRadarTurnRate(), 0.00001);
		assertEquals(bulletPower, intent.getBulletPower(), 0.00001);
	}
}
