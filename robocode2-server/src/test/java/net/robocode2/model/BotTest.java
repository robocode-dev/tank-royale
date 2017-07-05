package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class BotTest {

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
	public void constructorEmpty() {
		Bot bot = new Bot();

		assertEquals(0, bot.getId(), 0.0001);
		assertEquals(100, bot.getEnergy(), 0.0001);
		assertNull(bot.getPosition());
		assertEquals(0, bot.getDirection(), 0.0001);
		assertEquals(0, bot.getGunDirection(), 0.0001);
		assertEquals(0, bot.getRadarDirection(), 0.0001);
		assertEquals(0, bot.getSpeed(), 0.0001);
		assertEquals(0, bot.getGunHeat(), 0.0001);
		assertNull(bot.getScanField());
		assertNull(bot.getScore());
	}

	@Test
	public void constructorIBot() {
		assertReflectionEquals(initializedBot, new Bot(initializedBot));
	}

	@Test
	public void toImmutableBot() {
		assertReflectionEquals(initializedBot.toImmutableBot(), new Bot(initializedBot).toImmutableBot());
	}

	@Test
	public void setId() {
		Bot bot = new Bot();
		bot.setId(7913);

		assertEquals(7913, bot.getId(), 0.00001);
	}

	@Test
	public void setEnergy() {
		Bot bot = new Bot();
		bot.setEnergy(7913.7913);

		assertEquals(7913.7913, bot.getEnergy(), 0.00001);
	}

	@Test
	public void setPosition() {
		Bot bot = new Bot();
		Point position = new Point(12.34, 56.78);
		bot.setPosition(position);

		assertReflectionEquals(position, bot.getPosition());
	}

	@Test
	public void setDirection() {
		Bot bot = new Bot();
		bot.setDirection(7913.7913);

		assertEquals(7913.7913, bot.getDirection(), 0.00001);
	}

	@Test
	public void setGunDirection() {
		Bot bot = new Bot();
		bot.setGunDirection(7913.7913);

		assertEquals(7913.7913, bot.getGunDirection(), 0.00001);
	}

	@Test
	public void setRadarDirection() {
		Bot bot = new Bot();
		bot.setRadarDirection(7913.7913);

		assertEquals(7913.7913, bot.getRadarDirection(), 0.00001);
	}

	@Test
	public void setSpeed() {
		Bot bot = new Bot();
		bot.setSpeed(7913.7913);

		assertEquals(7913.7913, bot.getSpeed(), 0.00001);
	}

	@Test
	public void setGunHeat() {
		Bot bot = new Bot();
		bot.setGunHeat(7913.7913);

		assertEquals(7913.7913, bot.getGunHeat(), 0.00001);
	}

	@Test
	public void setScanField() {
		Bot bot = new Bot();
		ScanField scanField = new ScanField(12.34, 56.78);
		bot.setScanField(scanField);

		assertReflectionEquals(scanField, bot.getScanField());
	}

	@Test
	public void setScore() {
		Bot bot = new Bot();
		IScore score = initializedScore;
		bot.setScore(initializedBot.getScore());

		assertReflectionEquals(score, bot.getScore());
	}

	@Test
	public void addDamage() {
		Bot bot = new Bot();
		bot.setEnergy(100);

		bot.addDamage(10);
		assertEquals(100 - 10, bot.getEnergy(), 0.00001);

		bot.addDamage(12.45);
		assertEquals(100 - 10 - 12.45, bot.getEnergy(), 0.00001);
	}

	@Test
	public void changeEnergy() {
		Bot bot = new Bot();
		bot.setEnergy(100);

		bot.changeEnergy(10);
		assertEquals(100 + 10, bot.getEnergy(), 0.00001);

		bot.changeEnergy(-12.45);
		assertEquals(100 + 10 - 12.45, bot.getEnergy(), 0.00001);
	}

	@Test
	public void moveToNewPosition() {
		Bot bot = new Bot();

		Point position = new Point(12.45, 56.78);

		bot.setPosition(position);
		bot.setDirection(178);
		bot.setSpeed(8);
		bot.moveToNewPosition();

		Point newPosition = calcNewPosition(position, bot.getDirection(), bot.getSpeed());

		assertReflectionEquals(newPosition, bot.getPosition());

		position = new Point(567.94, 431.23);

		bot.setPosition(position);
		bot.setDirection(345.78);
		bot.setSpeed(-7.6);
		bot.moveToNewPosition();

		newPosition = calcNewPosition(position, bot.getDirection(), bot.getSpeed());

		assertReflectionEquals(newPosition, bot.getPosition());
	}

	@Test
	public void bounceBack() {
		Bot bot = new Bot();

		Point position = new Point(12.45, 56.78);

		bot.setPosition(position);
		bot.setDirection(178);
		bot.setSpeed(8);
		bot.bounceBack(3.4);

		Point newPosition = calcNewPosition(position, bot.getDirection(), -3.4);

		assertReflectionEquals(newPosition, bot.getPosition());

		position = new Point(567.94, 431.23);

		bot.setPosition(position);
		bot.setDirection(247.33);
		bot.setSpeed(0.0000001);
		bot.bounceBack(0.54);

		newPosition = calcNewPosition(position, bot.getDirection(), -0.54);

		assertReflectionEquals(newPosition, bot.getPosition());
	}

	private static Point calcNewPosition(Point position, double direction, double distance) {
		double angle = Math.toRadians(direction);
		double x = position.x + Math.cos(angle) * distance;
		double y = position.y + Math.sin(angle) * distance;
		return new Point(x, y);
	}
}
