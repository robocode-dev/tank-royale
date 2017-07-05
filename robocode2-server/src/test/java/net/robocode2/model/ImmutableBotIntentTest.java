package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableBotIntentTest {

	static BotIntent initializedBotIntent;

	@BeforeClass
	public static void initialize() {
		BotIntent intent = new BotIntent();

		intent.update(new IBotIntent() {
			@Override
			public Double getTargetSpeed() {
				return 1.234;
			}

			@Override
			public Double getDrivingTurnRate() {
				return 23.45;
			}

			@Override
			public Double getGunTurnRate() {
				return -56.74;
			}

			@Override
			public Double getRadarTurnRate() {
				return -66.6;
			}

			@Override
			public Double getBulletPower() {
				return 3.17;
			}
		});

		initializedBotIntent = intent;
	}

	@Test
	public void constructorParams() {
		ImmutableBotIntent intent = new ImmutableBotIntent(initializedBotIntent.getTargetSpeed(),
				initializedBotIntent.getDrivingTurnRate(), initializedBotIntent.getGunTurnRate(),
				initializedBotIntent.getRadarTurnRate(), initializedBotIntent.getBulletPower());

		assertEquals(initializedBotIntent.getTargetSpeed(), intent.getTargetSpeed());
		assertEquals(initializedBotIntent.getDrivingTurnRate(), intent.getDrivingTurnRate());
		assertEquals(initializedBotIntent.getGunTurnRate(), intent.getGunTurnRate());
		assertEquals(initializedBotIntent.getRadarTurnRate(), intent.getRadarTurnRate());
		assertEquals(initializedBotIntent.getBulletPower(), intent.getBulletPower());
	}

	@Test
	public void constructorIBotIntent() {
		ImmutableBotIntent intent = new ImmutableBotIntent(initializedBotIntent);

		assertEquals(initializedBotIntent.getTargetSpeed(), intent.getTargetSpeed());
		assertEquals(initializedBotIntent.getDrivingTurnRate(), intent.getDrivingTurnRate());
		assertEquals(initializedBotIntent.getGunTurnRate(), intent.getGunTurnRate());
		assertEquals(initializedBotIntent.getRadarTurnRate(), intent.getRadarTurnRate());
		assertEquals(initializedBotIntent.getBulletPower(), intent.getBulletPower());
	}
}
