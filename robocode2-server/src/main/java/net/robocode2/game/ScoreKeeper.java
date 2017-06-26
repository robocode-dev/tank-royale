package net.robocode2.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.robocode2.model.IRuleConstants;
import net.robocode2.model.ImmutableScore;
import net.robocode2.model.Score;

/**
 * Score utility class used for keeping track of the score for an individual bot in a game.
 * 
 * @author Flemming N. Larsen
 */
public class ScoreKeeper implements IRuleConstants {

	/** Set of bot identifiers */
	private final Set<Integer> botIds;

	/** Map from bot identifier to a DamageAndSurvival record */
	private final Map<Integer, DamageAndSurvival> damageAndSurvivals;

	/** Set of identifiers of bots alive */
	private final Set<Integer> botsAliveIds;

	/**
	 * Creates a new ScoreKeeper instance.
	 *
	 * @param botIds
	 *            is a set of bot identifiers, which cannot be null.
	 */
	public ScoreKeeper(Set<Integer> botIds) {
		this.botIds = new HashSet<>(botIds);
		this.botsAliveIds = new HashSet<>(botIds);
		this.damageAndSurvivals = new HashMap<>();
		populateDamageAndSurvivals();
	}

	public void reset() {
		damageAndSurvivals.clear();
		populateDamageAndSurvivals();
		botsAliveIds.clear();
	}

	private void populateDamageAndSurvivals() {
		for (int botId : botIds) {
			damageAndSurvivals.put(botId, new DamageAndSurvival());
		}
	}

	public ImmutableScore getScore(int botId) {
		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);

		Score score = new Score();
		score.setSurvival(SCORE_PER_SURVIVAL * damageRecord.getSurvivalCount());
		score.setLastSurvivorBonus(BONUS_PER_LAST_SURVIVOR * damageRecord.getLastSurvivorCount());

		score.setBulletDamage(SCORE_PER_BULLET_DAMAGE * damageRecord.getTotalBulletDamage());
		score.setRamDamage(SCORE_PER_RAM_DAMAGE * damageRecord.getTotalRamDamage());

		double bulletKillBonus = 0;
		for (int enemyId : damageRecord.getBulletKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			bulletKillBonus += BONUS_PER_BULLET_KILL * totalDamage;
		}
		score.setBulletKillBonus(bulletKillBonus);

		double ramKillBonus = 0;
		for (int enemyId : damageRecord.getRamKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			ramKillBonus += BONUS_PER_RAM_KILL * totalDamage;
		}
		score.setRamKillBonus(ramKillBonus);

		return score.toImmutableScore();
	}

	public void addBulletHit(int botId, int victimBotId, double damage, boolean kill) {
		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);

		damageRecord.addBulletDamage(victimBotId, damage);
		if (kill) {
			handleKill(victimBotId);

			damageRecord.addBulletKillEnemyId(victimBotId);
		}
	}

	public void addRamHit(int botId, int victimBotId, double damage, boolean kill) {
		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);

		damageRecord.addRamDamage(victimBotId, damage);
		if (kill) {
			handleKill(victimBotId);

			damageRecord.addRamKillEnemyId(victimBotId);
		}
	}

	private void handleKill(int killedBotId) {
		botsAliveIds.remove(killedBotId);

		for (int botId : botsAliveIds) {
			damageAndSurvivals.get(botId).incrementSurvivalCount();
		}

		if (botsAliveIds.size() == 1) {
			int survivorId = botsAliveIds.iterator().next();
			int deadCount = damageAndSurvivals.size() - botsAliveIds.size();

			damageAndSurvivals.get(survivorId).addLastSurvivorCount(deadCount);
		}
	}

	private static class DamageAndSurvival {

		int survivalCount;
		int lastSurvivorCount;

		Map<Integer, Double> bulletDamage = new HashMap<>();
		Map<Integer, Double> ramDamage = new HashMap<>();

		Set<Integer> bulletKillEnemyIds = new HashSet<>();
		Set<Integer> ramKillEnemyIds = new HashSet<>();

		public int getSurvivalCount() {
			return survivalCount;
		}

		public int getLastSurvivorCount() {
			return lastSurvivorCount;
		}

		public double getTotalBulletDamage() {
			double sum = 0;
			for (int enemyId : bulletDamage.keySet()) {
				sum += getBulletDamage(enemyId);
			}
			return sum;
		}

		public double getTotalRamDamage() {
			double sum = 0;
			for (int enemyId : ramDamage.keySet()) {
				sum += getRamDamage(enemyId);
			}
			return sum;
		}

		public double getBulletDamage(int enemyId) {
			Double sum = bulletDamage.get(enemyId);
			if (sum == null) {
				sum = 0.0;
			}
			return sum;
		}

		public double getRamDamage(int enemyId) {
			Double sum = ramDamage.get(enemyId);
			if (sum == null) {
				sum = 0.0;
			}
			return sum;
		}

		public Set<Integer> getBulletKillEnemyIds() {
			return Collections.unmodifiableSet(bulletKillEnemyIds);
		}

		public Set<Integer> getRamKillEnemyIds() {
			return Collections.unmodifiableSet(ramKillEnemyIds);
		}

		public void addBulletDamage(int enemyId, double damage) {
			double sum = getBulletDamage(enemyId) + damage;
			bulletDamage.put(enemyId, sum);
		}

		public void incrementSurvivalCount() {
			survivalCount++;
		}

		public void addLastSurvivorCount(int numberOfDeadEnemies) {
			lastSurvivorCount += numberOfDeadEnemies;
		}

		public void addRamDamage(int enemyId, double damage) {
			double sum = getRamDamage(enemyId) + damage;
			ramDamage.put(enemyId, sum);
		}

		public void addBulletKillEnemyId(int enemyId) {
			bulletKillEnemyIds.add(enemyId);
		}

		public void addRamKillEnemyId(int enemyId) {
			ramKillEnemyIds.add(enemyId);
		}
	}
}