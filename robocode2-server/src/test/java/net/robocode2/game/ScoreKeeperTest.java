package net.robocode2.game;

import static net.robocode2.model.RuleConstants.BONUS_PER_BULLET_KILL;
import static net.robocode2.model.RuleConstants.BONUS_PER_LAST_SURVIVOR;
import static net.robocode2.model.RuleConstants.BONUS_PER_RAM_KILL;
import static net.robocode2.model.RuleConstants.SCORE_PER_BULLET_DAMAGE;
import static net.robocode2.model.RuleConstants.SCORE_PER_RAM_DAMAGE;
import static net.robocode2.model.RuleConstants.SCORE_PER_SURVIVAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.robocode2.model.Score;

public class ScoreKeeperTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void constructor() {
		try {
			new ScoreKeeper(null);
			fail();
		} catch (NullPointerException e) {
		}

		ScoreKeeper sk = new ScoreKeeper(new HashSet<>(Arrays.asList(1, 2, 3)));

		testZeroScore(sk.getScore(1));
		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
	}

	@Test
	public void getScore() {
		ScoreKeeper sk = new ScoreKeeper(new HashSet<>(Arrays.asList(1, 2, 3)));

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
	public void clear() {
		ScoreKeeper sk = new ScoreKeeper(new HashSet<>(Arrays.asList(1, 2, 3)));

		sk.registerBulletHit(1, 2, 10, false);
		sk.registerRamHit(1, 2, 20, false);

		sk.registerBulletHit(2, 3, 10, false);
		sk.registerRamHit(2, 3, 20, false);

		sk.registerBulletHit(3, 1, 10, false);
		sk.registerRamHit(3, 1, 20, false);

		sk.clear();

		testZeroScore(sk.getScore(1));
		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
	}

	@Test
	public void registerBulletHit() {
		ScoreKeeper sk = new ScoreKeeper(new HashSet<>(Arrays.asList(1, 2, 3, 4)));

		// --- Bot 1 hits bot 2, no kill
		sk.registerBulletHit(1, 2, 7, false);

		Score s = sk.getScore(1);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
		testZeroScore(sk.getScore(4));

		// --- Bot 2 hits and kills bot 1

		sk.registerBulletHit(2, 1, 9, true);

		s = sk.getScore(1);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(9 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(9 * BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(9 * (SCORE_PER_BULLET_DAMAGE + BONUS_PER_BULLET_KILL) + 1 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 3 hits and kills bot 2

		sk.registerBulletHit(3, 2, 13, true);

		s = sk.getScore(1);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(9 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(9 * BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(9 * (SCORE_PER_BULLET_DAMAGE + BONUS_PER_BULLET_KILL) + 1 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(13 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(13 * BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(13 * (SCORE_PER_BULLET_DAMAGE + BONUS_PER_BULLET_KILL) + 2 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 4 hits and kills bot 3

		sk.registerBulletHit(4, 3, 5, true);

		s = sk.getScore(1);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_BULLET_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(9 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(9 * BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(9 * (SCORE_PER_BULLET_DAMAGE + BONUS_PER_BULLET_KILL) + 1 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(13 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(13 * BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(13 * (SCORE_PER_BULLET_DAMAGE + BONUS_PER_BULLET_KILL) + 2 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(5 * SCORE_PER_BULLET_DAMAGE, s.getBulletDamage(), 0.00001);
		assertEquals(5 * BONUS_PER_BULLET_KILL, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(3 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(3 * BONUS_PER_LAST_SURVIVOR, s.getLastSurvivorBonus(), 0.00001); // Only last bot gets this
		assertEquals(5 * (SCORE_PER_BULLET_DAMAGE + BONUS_PER_BULLET_KILL)
				+ 3 * (SCORE_PER_SURVIVAL + BONUS_PER_LAST_SURVIVOR), s.getTotalScore(), 00001);
	}

	@Test
	public void registerRamHit() {
		ScoreKeeper sk = new ScoreKeeper(new HashSet<>(Arrays.asList(1, 2, 3, 4)));

		// --- Bot 1 hits bot 2, no kill
		sk.registerRamHit(1, 2, 7, false);

		Score s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		testZeroScore(sk.getScore(2));
		testZeroScore(sk.getScore(3));
		testZeroScore(sk.getScore(4));

		// --- Bot 2 hits and kills bot 1

		sk.registerRamHit(2, 1, 9, true);

		s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(9 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(9 * BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(9 * (SCORE_PER_RAM_DAMAGE + BONUS_PER_RAM_KILL) + 1 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 3 hits and kills bot 2

		sk.registerRamHit(3, 2, 13, true);

		s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(9 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(9 * BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(9 * (SCORE_PER_RAM_DAMAGE + BONUS_PER_RAM_KILL) + 1 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(13 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(13 * BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(13 * (SCORE_PER_RAM_DAMAGE + BONUS_PER_RAM_KILL) + 2 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(0, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getTotalScore(), 00001);

		// --- Bot 4 hits and kills bot 3

		sk.registerRamHit(4, 3, 5, true);

		s = sk.getScore(1);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(0, s.getRamKillBonus(), 0.00001);
		assertEquals(0, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(7 * SCORE_PER_RAM_DAMAGE, s.getTotalScore(), 00001);

		s = sk.getScore(2);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(9 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(9 * BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		assertEquals(1 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(9 * (SCORE_PER_RAM_DAMAGE + BONUS_PER_RAM_KILL) + 1 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(3);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(13 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(13 * BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		assertEquals(2 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(0, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(13 * (SCORE_PER_RAM_DAMAGE + BONUS_PER_RAM_KILL) + 2 * SCORE_PER_SURVIVAL, s.getTotalScore(),
				00001);

		s = sk.getScore(4);
		assertEquals(0, s.getBulletDamage(), 0.00001);
		assertEquals(0, s.getBulletKillBonus(), 0.00001);
		assertEquals(5 * SCORE_PER_RAM_DAMAGE, s.getRamDamage(), 0.00001);
		assertEquals(5 * BONUS_PER_RAM_KILL, s.getRamKillBonus(), 0.00001);
		assertEquals(3 * SCORE_PER_SURVIVAL, s.getSurvival(), 0.00001);
		assertEquals(3 * BONUS_PER_LAST_SURVIVOR, s.getLastSurvivorBonus(), 0.00001);
		assertEquals(
				5 * (SCORE_PER_RAM_DAMAGE + BONUS_PER_RAM_KILL) + 3 * (SCORE_PER_SURVIVAL + BONUS_PER_LAST_SURVIVOR),
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
