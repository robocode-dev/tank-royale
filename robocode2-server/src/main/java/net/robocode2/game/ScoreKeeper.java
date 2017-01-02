package net.robocode2.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.robocode2.model.Score;
import net.robocode2.model.Score.Builder;

public class ScoreKeeper {

	private static final double SCORE_PER_SURVIVAL = 50;
	private static final double BONUS_PER_LAST_SURVIVOR = 10;
	private static final double SCORE_PER_BULLET_DAMAGE = 1;
	private static final double BONUS_PER_BULLET_KILL = 0.20;
	private static final double SCORE_PER_RAM_DAMAGE = 2;
	private static final double BONUS_PER_RAM_KILL = 0.30;

	private Map<Integer, DamageAndSurvival> damageAndSurvivals;

	private Set<Integer> botsAlive;

	public ScoreKeeper(Set<Integer> botIds) {
		reset(botIds);
	}

	public void reset(Set<Integer> botIds) {
		damageAndSurvivals.clear();
		for (int botId : botIds) {
			damageAndSurvivals.put(botId, new DamageAndSurvival());
		}
		botsAlive.clear();
	}

	public Score getScore(int botId) {
		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);

		Builder scoreBuilder = new Builder();
		scoreBuilder.setSurvival(SCORE_PER_SURVIVAL * damageRecord.getSurvivalCount());
		scoreBuilder.setLastSurvivorBonus(BONUS_PER_LAST_SURVIVOR * damageRecord.getLastSurvivorCount());

		scoreBuilder.setBulletDamage(SCORE_PER_BULLET_DAMAGE * damageRecord.getTotalBulletDamage());
		scoreBuilder.setRamDamage(SCORE_PER_RAM_DAMAGE * damageRecord.getTotalRamDamage());

		double bulletKillBonus = 0;
		for (int enemyId : damageRecord.getBulletKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			bulletKillBonus += BONUS_PER_BULLET_KILL * totalDamage;
		}
		scoreBuilder.setBulletKillBonus(bulletKillBonus);

		double ramKillBonus = 0;
		for (int enemyId : damageRecord.getRamKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			ramKillBonus += BONUS_PER_RAM_KILL * totalDamage;
		}
		scoreBuilder.setRamKillBonus(ramKillBonus);

		return scoreBuilder.build();
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
		botsAlive.remove(killedBotId);

		for (int botId : botsAlive) {
			damageAndSurvivals.get(botId).incrementSurvivalCount();
		}

		if (botsAlive.size() == 1) {
			int survivorId = botsAlive.iterator().next();
			int deadCount = damageAndSurvivals.size() - botsAlive.size();

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