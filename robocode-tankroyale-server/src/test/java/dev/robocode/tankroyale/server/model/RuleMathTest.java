package dev.robocode.tankroyale.server.model;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("IntegerDivisionInFloatingPointContext")
public class RuleMathTest {

	@Test
	public void calcNewBotSpeed() {
		assertEquals(0, RuleMath.calcNewBotSpeed(0, 0), 0);

		Assert.assertEquals(RuleConstants.MAX_FORWARD_SPEED, RuleMath.calcNewBotSpeed(RuleConstants.MAX_FORWARD_SPEED - 0.1, RuleConstants.MAX_FORWARD_SPEED), 0);
		Assert.assertEquals(-RuleConstants.MAX_FORWARD_SPEED, RuleMath.calcNewBotSpeed(-RuleConstants.MAX_FORWARD_SPEED + 0.1, -RuleConstants.MAX_FORWARD_SPEED), 0);

		Assert.assertEquals(RuleConstants.MAX_FORWARD_SPEED, RuleMath.calcNewBotSpeed(RuleConstants.MAX_FORWARD_SPEED - 0.1, RuleConstants.MAX_FORWARD_SPEED + 5), 0);
		Assert.assertEquals(-RuleConstants.MAX_FORWARD_SPEED, RuleMath.calcNewBotSpeed(-RuleConstants.MAX_FORWARD_SPEED + 0.1, -RuleConstants.MAX_FORWARD_SPEED - 5), 0);

		Assert.assertEquals(RuleConstants.ACCELERATION, RuleMath.calcNewBotSpeed(0, RuleConstants.ACCELERATION), 0);
		Assert.assertEquals(RuleConstants.ACCELERATION - 0.1, RuleMath.calcNewBotSpeed(0, RuleConstants.ACCELERATION - 0.1), 0);
		Assert.assertEquals(RuleConstants.ACCELERATION, RuleMath.calcNewBotSpeed(0, RuleConstants.ACCELERATION + 0.1), 0);

		Assert.assertEquals(RuleConstants.DECELERATION, RuleMath.calcNewBotSpeed(0, RuleConstants.DECELERATION), 0);
		Assert.assertEquals(RuleConstants.DECELERATION + 0.1, RuleMath.calcNewBotSpeed(0, RuleConstants.DECELERATION + 0.1), 0);
		Assert.assertEquals(RuleConstants.DECELERATION, RuleMath.calcNewBotSpeed(0, RuleConstants.DECELERATION - 0.1), 0);

		assertEquals(4.3, RuleMath.calcNewBotSpeed(3.7, 4.3), 0);
		assertEquals(1.9, RuleMath.calcNewBotSpeed(3.7, 1.9), 0);

		Assert.assertEquals(3.7 + RuleConstants.ACCELERATION, RuleMath.calcNewBotSpeed(3.7, 8), 0);
		Assert.assertEquals(3.7 + RuleConstants.DECELERATION, RuleMath.calcNewBotSpeed(3.7, 0), 0);

		assertEquals(-4.3, RuleMath.calcNewBotSpeed(-3.7, -4.3), 0);
		assertEquals(-1.9, RuleMath.calcNewBotSpeed(-3.7, -1.9), 0);

		Assert.assertEquals(-3.7 - RuleConstants.ACCELERATION, RuleMath.calcNewBotSpeed(-3.7, -8), 0);
		Assert.assertEquals(-3.7 - RuleConstants.DECELERATION, RuleMath.calcNewBotSpeed(-3.7, 0), 0);
	}

	@Test
	public void limitTurnRate() {
		assertEquals(0, RuleMath.limitTurnRate(0, 0), 0);
		assertEquals(0, RuleMath.limitTurnRate(0, 8), 0);
		assertEquals(0, RuleMath.limitTurnRate(0, -8), 0);

		assertEquals(4, RuleMath.limitTurnRate(4, 0), 0);
		assertEquals(4, RuleMath.limitTurnRate(4, 4), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 8, RuleMath.limitTurnRate(4, 8), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 10, RuleMath.limitTurnRate(4, 10), 0);
		assertEquals(4, RuleMath.limitTurnRate(4, -4), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 8, RuleMath.limitTurnRate(4, -8), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 10, RuleMath.limitTurnRate(4, -10), 0);

		assertEquals(8, RuleMath.limitTurnRate(8, 0), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 4, RuleMath.limitTurnRate(8, 4), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 8, RuleMath.limitTurnRate(8, 8), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 10, RuleMath.limitTurnRate(8, 10), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 4, RuleMath.limitTurnRate(8, -4), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 8, RuleMath.limitTurnRate(8, -8), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 10, RuleMath.limitTurnRate(8, -10), 0);

		assertEquals(10, RuleMath.limitTurnRate(10, 0), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 4, RuleMath.limitTurnRate(10, 4), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 8, RuleMath.limitTurnRate(10, 8), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 10, RuleMath.limitTurnRate(10, 10), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 4, RuleMath.limitTurnRate(10, -4), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 8, RuleMath.limitTurnRate(10, -8), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 10, RuleMath.limitTurnRate(10, -10), 0);

		assertEquals(-4, RuleMath.limitTurnRate(-4, 0), 0);
		assertEquals(-4, RuleMath.limitTurnRate(-4, 4), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 8), RuleMath.limitTurnRate(-4, 8), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 10), RuleMath.limitTurnRate(-4, 10), 0);
		assertEquals(-4, RuleMath.limitTurnRate(-4, -4), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 8), RuleMath.limitTurnRate(-4, -8), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 10), RuleMath.limitTurnRate(-4, -10), 0);

		assertEquals(-8, RuleMath.limitTurnRate(-8, 0), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 4), RuleMath.limitTurnRate(-8, 4), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 8), RuleMath.limitTurnRate(-8, 8), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 10), RuleMath.limitTurnRate(-8, 10), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 4), RuleMath.limitTurnRate(-8, -4), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 8), RuleMath.limitTurnRate(-8, -8), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 10), RuleMath.limitTurnRate(-8, -10), 0);

		assertEquals(-10, RuleMath.limitTurnRate(-10, 0), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 4), RuleMath.limitTurnRate(-10, 4), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 8), RuleMath.limitTurnRate(-10, 8), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 10), RuleMath.limitTurnRate(-10, 10), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 4), RuleMath.limitTurnRate(-10, -4), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 8), RuleMath.limitTurnRate(-10, -8), 0);
		Assert.assertEquals(-(RuleConstants.MAX_TURN_RATE - 0.75 * 10), RuleMath.limitTurnRate(-10, -10), 0);
	}

	@Test
	public void limitGunTurnRate() {
		assertEquals(0, RuleMath.limitGunTurnRate(0), 0);
		assertEquals(10, RuleMath.limitGunTurnRate(10), 0);
		assertEquals(-10, RuleMath.limitGunTurnRate(-10), 0);

		Assert.assertEquals(RuleConstants.MAX_GUN_TURN_RATE, RuleMath.limitGunTurnRate(RuleConstants.MAX_GUN_TURN_RATE), 0);
		Assert.assertEquals(-RuleConstants.MAX_GUN_TURN_RATE, RuleMath.limitGunTurnRate(-RuleConstants.MAX_GUN_TURN_RATE), 0);

		Assert.assertEquals(RuleConstants.MAX_GUN_TURN_RATE, RuleMath.limitGunTurnRate(RuleConstants.MAX_GUN_TURN_RATE + 0.5), 0);
		Assert.assertEquals(-RuleConstants.MAX_GUN_TURN_RATE, RuleMath.limitGunTurnRate(-RuleConstants.MAX_GUN_TURN_RATE - 0.5), 0);
	}

	@Test
	public void limitRadarTurnRate() {
		assertEquals(0, RuleMath.limitRadarTurnRate(0), 0);
		assertEquals(10, RuleMath.limitRadarTurnRate(10), 0);
		assertEquals(-10, RuleMath.limitRadarTurnRate(-10), 0);

		Assert.assertEquals(RuleConstants.MAX_RADAR_TURN_RATE, RuleMath.limitRadarTurnRate(RuleConstants.MAX_RADAR_TURN_RATE), 0);
		Assert.assertEquals(-RuleConstants.MAX_RADAR_TURN_RATE, RuleMath.limitRadarTurnRate(-RuleConstants.MAX_RADAR_TURN_RATE), 0);

		Assert.assertEquals(RuleConstants.MAX_RADAR_TURN_RATE, RuleMath.limitRadarTurnRate(RuleConstants.MAX_RADAR_TURN_RATE + 0.5), 0);
		Assert.assertEquals(-RuleConstants.MAX_RADAR_TURN_RATE, RuleMath.limitRadarTurnRate(-RuleConstants.MAX_RADAR_TURN_RATE - 0.5), 0);
	}

	@Test
	public void calcMaxTurnRate() {
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 0, RuleMath.calcMaxTurnRate(0), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 5.5, RuleMath.calcMaxTurnRate(5.5), 0);
		Assert.assertEquals(RuleConstants.MAX_TURN_RATE - 0.75 * 4.3, RuleMath.calcMaxTurnRate(-4.3), 0);
	}

	@Test
	public void calcWallDamage() {
		assertEquals(0, RuleMath.calcWallDamage(0), 0);

		assertEquals(0, RuleMath.calcWallDamage(0.5), 0);
		assertEquals(0, RuleMath.calcWallDamage(1), 0);
		assertEquals(0, RuleMath.calcWallDamage(1.5), 0);
		assertEquals(2.0 / 2 - 1, RuleMath.calcWallDamage(2), 0);
		assertEquals(2.5 / 2 - 1, RuleMath.calcWallDamage(2.5), 0);
		assertEquals(4.3 / 2 - 1, RuleMath.calcWallDamage(4.3), 0);
		assertEquals(8.0 / 2 - 1, RuleMath.calcWallDamage(8), 0);
		assertEquals(10 / 2 - 1, RuleMath.calcWallDamage(10), 0);

		assertEquals(0, RuleMath.calcWallDamage(-0.5), 0);
		assertEquals(0, RuleMath.calcWallDamage(-1), 0);
		assertEquals(0, RuleMath.calcWallDamage(-1.5), 0);
		assertEquals(2.0 / 2 - 1, RuleMath.calcWallDamage(-2), 0);
		assertEquals(2.5 / 2 - 1, RuleMath.calcWallDamage(-2.5), 0);
		assertEquals(4.3 / 2 - 1, RuleMath.calcWallDamage(-4.3), 0);
		assertEquals(8 / 2 - 1, RuleMath.calcWallDamage(-8), 0);
		assertEquals(10 / 2 - 1, RuleMath.calcWallDamage(-10), 0);
	}

	@Test
	public void calcBulletSpeed() {
		assertEquals(20, RuleMath.calcBulletSpeed(0), 0);
		assertEquals(20 - 3 * 0.1, RuleMath.calcBulletSpeed(0.1), 0);
		assertEquals(20 - 3 * 1.57, RuleMath.calcBulletSpeed(1.57), 0);
		assertEquals(20 - 3 * 3, RuleMath.calcBulletSpeed(3), 0);
	}

	@Test
	public void calcBulletDamage() {
		assertEquals(0, RuleMath.calcBulletDamage(0), 0);
		assertEquals(4 * 0.1, RuleMath.calcBulletDamage(0.1), 0);
		assertEquals(4 * 1.57 + 2 * (1.57 - 1), RuleMath.calcBulletDamage(1.57), 0);
		assertEquals(4 * 3 + 2 * (3 - 1), RuleMath.calcBulletDamage(3), 0);
	}

	@Test
	public void calcGunHeat() {
		assertEquals(1 + (0.0 / 5), RuleMath.calcGunHeat(0), 0);
		assertEquals(1 + (0.1 / 5), RuleMath.calcGunHeat(0.1), 0);
		assertEquals(1 + (1.57 / 5), RuleMath.calcGunHeat(1.57), 0);
		assertEquals(1 + (3.0 / 5), RuleMath.calcGunHeat(3), 0);
	}
}