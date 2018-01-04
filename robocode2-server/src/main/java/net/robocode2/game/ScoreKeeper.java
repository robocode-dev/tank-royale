package net.robocode2.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.robocode2.model.IRuleConstants;
import net.robocode2.model.Score;
import net.robocode2.model.Score.ScoreBuilder;

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

	/**
	 * Clears all scores for all bots.
	 */
	public void clear() {
		damageAndSurvivals.clear();
		populateDamageAndSurvivals();
		botsAliveIds.clear();
	}

	/**
	 * Populates the map containing the DamageAndSurvival record for each bot.
	 */
	private void populateDamageAndSurvivals() {
		for (int botId : botIds) {
			damageAndSurvivals.put(botId, new DamageAndSurvival());
		}
	}

	/**
	 * Returns the score record for a specific bot.
	 *
	 * @param botId
	 *            is the identifier of the bot.
	 * @return a score record.
	 */
	public Score getScore(int botId) {
		ScoreBuilder builder = Score.builder();

		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);
		
		builder.survival(SCORE_PER_SURVIVAL * damageRecord.getSurvivalCount());
		builder.lastSurvivorBonus(BONUS_PER_LAST_SURVIVOR * damageRecord.getLastSurvivorCount());

		builder.bulletDamage(SCORE_PER_BULLET_DAMAGE * damageRecord.getTotalBulletDamage());
		builder.ramDamage(SCORE_PER_RAM_DAMAGE * damageRecord.getTotalRamDamage());

		double bulletKillBonus = 0;
		for (int enemyId : damageRecord.getBulletKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			bulletKillBonus += BONUS_PER_BULLET_KILL * totalDamage;
		}
		builder.bulletKillBonus(bulletKillBonus);

		double ramKillBonus = 0;
		for (int enemyId : damageRecord.getRamKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			ramKillBonus += BONUS_PER_RAM_KILL * totalDamage;
		}
		builder.ramKillBonus(ramKillBonus);

		return builder.build();
	}

	/**
	 * Registers a bullet hit.
	 *
	 * @param botId
	 *            is the identifier of the bot that hit another bot
	 * @param victimBotId
	 *            is the identifier of the victim bot that got hit by the bullet
	 * @param damage
	 *            is the damage that the victim bot receives
	 * @param kill
	 *            is a flag specifying, if the bot got killed by this bullet
	 */
	public void registerBulletHit(int botId, int victimBotId, double damage, boolean kill) {
		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);

		damageRecord.addBulletDamage(victimBotId, damage);
		if (kill) {
			handleKill(victimBotId);

			damageRecord.addBulletKillEnemyId(victimBotId);
		}
	}

	/**
	 * Registers a ram hit.
	 *
	 * @param botId
	 *            is the identifier of the bot that rammed another bot
	 * @param victimBotId
	 *            is the identifier of the victim bot that got rammed
	 * @param damage
	 *            is the damage that the victim bot receives
	 * @param kill
	 *            is a flag specifying, if the bot got killed by the ramming
	 */
	public void registerRamHit(int botId, int victimBotId, double damage, boolean kill) {
		DamageAndSurvival damageRecord = damageAndSurvivals.get(botId);

		damageRecord.addRamDamage(victimBotId, damage);
		if (kill) {
			handleKill(victimBotId);

			damageRecord.addRamKillEnemyId(victimBotId);
		}
	}

	/**
	 * Handles a kill.
	 *
	 * @param killedBotId
	 *            is the identifier of the bot that has been killed
	 */
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

	/**
	 * Dammage and survival record required to calculate the score of the individual bot.
	 * 
	 * @author Flemming N. Larsen
	 */
	private static class DamageAndSurvival {

		private int survivalCount;
		private int lastSurvivorCount;

		private final Map<Integer, Double> bulletDamage = new HashMap<>();
		private final Map<Integer, Double> ramDamage = new HashMap<>();

		private final Set<Integer> bulletKillEnemyIds = new HashSet<>();
		private final Set<Integer> ramKillEnemyIds = new HashSet<>();

		/**
		 * Returns the survival count, which is the number of rounds where the bot has survived.
		 * 
		 * @return the survival count.
		 */
		public int getSurvivalCount() {
			return survivalCount;
		}

		/**
		 * Returns the last survivor count, which is the number of bots that was killed, before this bot became the last
		 * survivor.
		 * 
		 * @return the last survivor count.
		 */
		public int getLastSurvivorCount() {
			return lastSurvivorCount;
		}

		/**
		 * Returns the total bullet damage dealth by this bot to other bots.
		 * 
		 * @return the total bullet damage.
		 */
		public double getTotalBulletDamage() {
			double sum = 0;
			for (int enemyId : bulletDamage.keySet()) {
				sum += getBulletDamage(enemyId);
			}
			return sum;
		}

		/**
		 * Returns the total ram damage dealth by this bot to other bots.
		 * 
		 * @return the total ram damage.
		 */
		public double getTotalRamDamage() {
			double sum = 0;
			for (int enemyId : ramDamage.keySet()) {
				sum += getRamDamage(enemyId);
			}
			return sum;
		}

		/**
		 * Returns the bullet damage dealth by this bot to specific bot.
		 * 
		 * @param enemyId
		 *            is the enemy bot to retrieve the damage for.
		 * @return the bullet damage dealth to a specific bot.
		 */
		public double getBulletDamage(int enemyId) {
			Double sum = bulletDamage.get(enemyId);
			if (sum == null) {
				sum = 0.0;
			}
			return sum;
		}

		/**
		 * Returns the ram damage dealth by this bot to specific bot.
		 * 
		 * @param enemyId
		 *            is the enemy bot to retrieve the damage for.
		 * @return the ram damage dealth to a specific bot.
		 */
		public double getRamDamage(int enemyId) {
			Double sum = ramDamage.get(enemyId);
			if (sum == null) {
				sum = 0.0;
			}
			return sum;
		}

		/**
		 * Returns a set of identifiers of all enemy bot that this bot has killed by bullets.
		 * 
		 * @return a set of enemy bot identifiers.
		 */
		public Set<Integer> getBulletKillEnemyIds() {
			return Collections.unmodifiableSet(bulletKillEnemyIds);
		}

		/**
		 * Returns a set of identifiers of all enemy bot that this bot has killed by ramming.
		 * 
		 * @return a set of enemy bot identifiers.
		 */
		public Set<Integer> getRamKillEnemyIds() {
			return Collections.unmodifiableSet(ramKillEnemyIds);
		}

		/**
		 * Adds bullet damage to a specific enemy bot.
		 *
		 * @param enemyId
		 *            is the identifier of the enemy bot
		 * @param damage
		 *            is the amount of damage that the enemy bot has received
		 */
		public void addBulletDamage(int enemyId, double damage) {
			double sum = getBulletDamage(enemyId) + damage;
			bulletDamage.put(enemyId, sum);
		}

		/**
		 * Adds ram damage to a specific enemy bot.
		 *
		 * @param enemyId
		 *            is the identifier of the enemy bot
		 * @param damage
		 *            is the amount of damage that the enemy bot has received
		 */
		public void addRamDamage(int enemyId, double damage) {
			double sum = getRamDamage(enemyId) + damage;
			ramDamage.put(enemyId, sum);
		}

		/**
		 * Increment the survival count, meaning that this bot has survived an additional round.
		 */
		public void incrementSurvivalCount() {
			survivalCount++;
		}

		/**
		 * Add number of dead enemies to the last survivor count, which only counts, if this bot becomes the last
		 * survivor.
		 *
		 * @param numberOfDeadEnemies
		 *            is the number of dead bots that must be added to the last survivor count.
		 */
		public void addLastSurvivorCount(int numberOfDeadEnemies) {
			lastSurvivorCount += numberOfDeadEnemies;
		}

		/**
		 * Adds the identifier of an enemy bot to the set over bots killed by a bullet from this bot.
		 * 
		 * @param enemyId
		 *            is the identifier of the enemy bot that was killed by this bot
		 */
		public void addBulletKillEnemyId(int enemyId) {
			bulletKillEnemyIds.add(enemyId);
		}

		/**
		 * Adds the identifier of an enemy bot to the set over bots killed by ramming by this bot.
		 * 
		 * @param enemyId
		 *            is the identifier of the enemy bot that was killed by this bot
		 */
		public void addRamKillEnemyId(int enemyId) {
			ramKillEnemyIds.add(enemyId);
		}
	}
}