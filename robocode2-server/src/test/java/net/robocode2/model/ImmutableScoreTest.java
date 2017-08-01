package net.robocode2.model;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableScoreTest {

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

		initializedScore = score;
	}

	@Test
	public void constructorIScore() {
		ImmutableScore score = new ImmutableScore(initializedScore);

		assertEquals(initializedScore.getBulletDamage(), score.getBulletDamage(), 0.00001);
		assertEquals(initializedScore.getBulletKillBonus(), score.getBulletKillBonus(), 0.00001);
		assertEquals(initializedScore.getRamDamage(), score.getRamDamage(), 0.00001);
		assertEquals(initializedScore.getRamKillBonus(), score.getRamKillBonus(), 0.00001);
		assertEquals(initializedScore.getSurvival(), score.getSurvival(), 0.00001);
		assertEquals(initializedScore.getLastSurvivorBonus(), score.getLastSurvivorBonus(), 0.00001);
	}
}
