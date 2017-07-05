package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableBotTest {

	static Bot initializedBot;
	static Score initializedScore;

	@BeforeClass
	public static void initialize() {
		Score score = new Score();
		score.setBulletDamage(3.48);
		score.setBulletKillBonus(0.3);
		score.setRamDamage(7.7);
		score.setRamKillBonus(0.56);
		score.setSurvival(17.09);
		score.setLastSurvivorBonus(4.3);

		Bot bot = new Bot();
		bot.setId(7913);
		bot.setEnergy(89.27);
		bot.setPosition(new Point(123.75, 567.125));
		bot.setDirection(47.3);
		bot.setGunDirection(158.9);
		bot.setRadarDirection(235.11);
		bot.setSpeed(7.5);
		bot.setGunHeat(2.71);
		bot.setScanField(new ScanField(123.45, 1200));
		bot.setScore(score);

		initializedBot = bot;
		initializedScore = score;
	}

	@Test
	public void constructorParams() {
		ImmutableBot bot = new ImmutableBot(initializedBot.getId(), initializedBot.getEnergy(),
				initializedBot.getPosition(), initializedBot.getDirection(), initializedBot.getGunDirection(),
				initializedBot.getRadarDirection(), initializedBot.getSpeed(), initializedBot.getGunHeat(),
				initializedBot.getScanField(), initializedBot.getScore());

		assertEquals(initializedBot.getId(), bot.getId());
		assertEquals(initializedBot.getEnergy(), bot.getEnergy(), 0.0001);
		assertEquals(initializedBot.getPosition(), bot.getPosition());
		assertEquals(initializedBot.getDirection(), bot.getDirection(), 0.0001);
		assertEquals(initializedBot.getGunDirection(), bot.getGunDirection(), 0.0001);
		assertEquals(initializedBot.getRadarDirection(), bot.getRadarDirection(), 0.0001);
		assertEquals(initializedBot.getSpeed(), bot.getSpeed(), 0.0001);
		assertEquals(initializedBot.getGunHeat(), bot.getGunHeat(), 0.0001);
		assertEquals(initializedBot.getScanField(), bot.getScanField());
		assertEquals(initializedBot.getScore(), bot.getScore());
	}

	@Test
	public void constructorIBot() {
		ImmutableBot bot = new ImmutableBot(initializedBot);

		assertEquals(initializedBot.getId(), bot.getId());
		assertEquals(initializedBot.getEnergy(), bot.getEnergy(), 0.0001);
		assertEquals(initializedBot.getPosition(), bot.getPosition());
		assertEquals(initializedBot.getDirection(), bot.getDirection(), 0.0001);
		assertEquals(initializedBot.getGunDirection(), bot.getGunDirection(), 0.0001);
		assertEquals(initializedBot.getRadarDirection(), bot.getRadarDirection(), 0.0001);
		assertEquals(initializedBot.getSpeed(), bot.getSpeed(), 0.0001);
		assertEquals(initializedBot.getGunHeat(), bot.getGunHeat(), 0.0001);
		assertEquals(initializedBot.getScanField(), bot.getScanField());
		assertEquals(initializedBot.getScore(), bot.getScore());
	}
}
