package dev.robocode.tankroyale.server.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import dev.robocode.tankroyale.server.engine.ScoreTracker;
import dev.robocode.tankroyale.server.model.RuleConstants;
import dev.robocode.tankroyale.server.model.Score;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScoreTrackerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void constructor() {
		try {
			new ScoreTracker(null);
			fail();
		} catch (NullPointerException e) {
		}

		ScoreTracker sk = new ScoreTracker(new HashSet<>(Arrays.asList(1, 2, 3)));

		testZeroScore(sk.getScore(1));
		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
	}

	@Test
	public void getScore() {
		ScoreTracker sk = new ScoreTracker(new HashSet<>(Arrays.asList(1, 2, 3)));

		assertNotNull(sk.getScore(1));
		assertNotNull(sk.getScore(2));
		assertNotNull(sk.getScore(3));

		try {
			sk.getScore(0);
			fail();
		} catch (NullPointerException e) {
		}

		try {
			sk.getScore(4);
			fail();
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void finalizeRound() {
		ScoreTracker sk = new ScoreTracker(new HashSet<>(Arrays.asList(1, 2, 3)));

		sk.registerBulletHit(1, 2, 10, false);
		sk.registerRamHit(1, 2, 20, false);
		sk.registerBulletHit(2, 3, 10, false);
		sk.registerRamHit(2, 3, 20, false);

		sk.registerBulletHit(3, 1, 10, false);
		sk.registerRamHit(3, 1, 20, false);

		Assert.assertEquals(10 * RuleConstants.SCORE_PER_BULLET_DAMAGE + 20 * RuleConstants.SCORE_PER_RAM_DAMAGE, sk.getScore(1).getTotalScore(), 0.0001);
		Assert.assertEquals(10 * RuleConstants.SCORE_PER_BULLET_DAMAGE + 20 * RuleConstants.SCORE_PER_RAM_DAMAGE, sk.getScore(2).getTotalScore(), 0.0001);
		Assert.assertEquals(10 * RuleConstants.SCORE_PER_BULLET_DAMAGE + 20 * RuleConstants.SCORE_PER_RAM_DAMAGE, sk.getScore(3).getTotalScore(), 0.0001);
	}

	@Test
	public void registerBulletHit() {
		ScoreTracker sk = new ScoreTracker(new HashSet<>(Arrays.asList(1, 2, 3, 4)));

		// --- Bot 1 hits bot 2, no kill
		sk.registerBulletHit(1, 2, 7, false);

		Score s = sk.getScore(1);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
		testZeroScore(sk.getScore(4));

		// --- Bot 2 hits and kills bot 1

		sk.registerBulletHit(2, 1, 9, true);
		sk.registerBotDeath(1);

		s = sk.getScore(1);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		Assert.assertEquals(9 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(9 * (RuleConstants.SCORE_PER_BULLET_DAMAGE + RuleConstants.BONUS_PER_BULLET_KILL) + 1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 3 hits and kills bot 2

		sk.registerBulletHit(3, 2, 13, true);
		sk.registerBotDeath(2);

		s = sk.getScore(1);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		Assert.assertEquals(9 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(9 * (RuleConstants.SCORE_PER_BULLET_DAMAGE + RuleConstants.BONUS_PER_BULLET_KILL) + 1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		Assert.assertEquals(13 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		Assert.assertEquals(13 * RuleConstants.BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(13 * (RuleConstants.SCORE_PER_BULLET_DAMAGE + RuleConstants.BONUS_PER_BULLET_KILL) + 2 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 4 hits and kills bot 3

		sk.registerBulletHit(4, 3, 5, true);
		sk.registerBotDeath(3);

		s = sk.getScore(1);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		Assert.assertEquals(9 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(9 * (RuleConstants.SCORE_PER_BULLET_DAMAGE + RuleConstants.BONUS_PER_BULLET_KILL) + 1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		Assert.assertEquals(13 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		Assert.assertEquals(13 * RuleConstants.BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(13 * (RuleConstants.SCORE_PER_BULLET_DAMAGE + RuleConstants.BONUS_PER_BULLET_KILL) + 2 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		Assert.assertEquals(5 * RuleConstants.SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		Assert.assertEquals(5 * RuleConstants.BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(3 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		Assert.assertEquals(3 * RuleConstants.BONUS_PER_LAST_SURVIVOR, s.getLastSurvivorBonus(), 0.00001); // Only last bot gets this
		Assert.assertEquals(5 * (RuleConstants.SCORE_PER_BULLET_DAMAGE + RuleConstants.BONUS_PER_BULLET_KILL)
				+ 3 * (RuleConstants.SCORE_PER_SURVIVAL + RuleConstants.BONUS_PER_LAST_SURVIVOR), s.getTotalScore(), 00001);
	}

	@Test
	public void registerRamHit() {
		ScoreTracker sk = new ScoreTracker(new HashSet<>(Arrays.asList(1, 2, 3, 4)));

		// --- Bot 1 hits bot 2, no kill
		sk.registerRamHit(1, 2, 7, false);

		Score s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
		testZeroScore(sk.getScore(4));

		// --- Bot 2 hits and kills bot 1

		sk.registerRamHit(2, 1, 9, true);
		sk.registerBotDeath(1);

		s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(9 * (RuleConstants.SCORE_PER_RAM_DAMAGE + RuleConstants.BONUS_PER_RAM_KILL) + 1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 3 hits and kills bot 2

		sk.registerRamHit(3, 2, 13, true);
		sk.registerBotDeath(2);

		s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(9 * (RuleConstants.SCORE_PER_RAM_DAMAGE + RuleConstants.BONUS_PER_RAM_KILL) + 1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(13 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		Assert.assertEquals(13 * RuleConstants.BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(13 * (RuleConstants.SCORE_PER_RAM_DAMAGE + RuleConstants.BONUS_PER_RAM_KILL) + 2 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 4 hits and kills bot 3

		sk.registerRamHit(4, 3, 5, true);
		sk.registerBotDeath(3);

		s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(7 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		Assert.assertEquals(9 * RuleConstants.BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(1 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(9 * (RuleConstants.SCORE_PER_RAM_DAMAGE + RuleConstants.BONUS_PER_RAM_KILL) + 1 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(13 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		Assert.assertEquals(13 * RuleConstants.BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(2 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(13 * (RuleConstants.SCORE_PER_RAM_DAMAGE + RuleConstants.BONUS_PER_RAM_KILL) + 2 * RuleConstants.SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		Assert.assertEquals(5 * RuleConstants.SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		Assert.assertEquals(5 * RuleConstants.BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		Assert.assertEquals(3 * RuleConstants.SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		Assert.assertEquals(3 * RuleConstants.BONUS_PER_LAST_SURVIVOR, s.getLastSurvivorBonus(), 0.00001);
		Assert.assertEquals(
				5 * (RuleConstants.SCORE_PER_RAM_DAMAGE + RuleConstants.BONUS_PER_RAM_KILL) + 3 * (RuleConstants.SCORE_PER_SURVIVAL + RuleConstants.BONUS_PER_LAST_SURVIVOR),
				s.getTotalScore(), 00001);
	}

	private void testZeroScore(Score score) {
		assertEquals(0, score.getBulletDamage(), 0.00001);
		assertEquals(0, score.getBulletKillBonus(), 0.00001);
		assertEquals(0, score.getLastSurvivorBonus(), 0.00001);
		assertEquals(0, score.getRamDamage(), 0.00001);
		assertEquals(0, score.getRamKillBonus(), 0.00001);
		assertEquals(0, score.getSurvival(), 0.00001);
		assertEquals(0, score.getTotalScore(), 0.00001);
	}
}
