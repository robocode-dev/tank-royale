package net.robocode2.engine;

import java.util.*;

import net.robocode2.model.RuleConstants;
import net.robocode2.model.Score;
import net.robocode2.model.Score.ScoreBuilder;

/**
 * Score utility class used for keeping track of the score for an individual bot in a game.
 * 
 * @author Flemming N. Larsen
 */
public class ScoreTracker {

	/** Set of bot identifiers */
	private final Set<Integer> botIds;

	/** Map from bot identifier to a bot record */
	private final Map<Integer, BotRecord> botRecords;

	/** Set of identifiers of bots alive */
	private final Set<Integer> botsAliveIds;

	/** 1st places */
	private final Map<Integer /* Bot ID */, Integer> place1st = new HashMap<>();
	/** 2nd places */
	private final Map<Integer /* Bot ID */, Integer> place2nd = new HashMap<>();
	/** 3rd places */
	private final Map<Integer /* Bot ID */, Integer> place3rd = new HashMap<>();

	/**
	 * Creates a new ScoreTracker instance.
	 *
	 * @param botIds
	 *            is a set of bot identifiers, which cannot be null.
	 */
	public ScoreTracker(Set<Integer> botIds) {
		this.botIds = new HashSet<>(botIds);
		this.botsAliveIds = new HashSet<>(botIds);
		this.botRecords = new HashMap<>();
		initializeDamageAndSurvivals();
	}

	void prepareRound() {
		this.botsAliveIds.addAll(botIds);
	}

	/**
	 * Returns the current results ordered with highest total scores first.
	 *
	 * @return a list of scores.
	 */
	List<Score> getResults() {
		List<Score> scores = getBotScores();
		for (int i = 0; i < scores.size(); i++) {
			Score score = scores.get(i);
			int botId = score.getBotId();
			Integer firstPlaces = place1st.get(botId);
			Integer secondPlaces = place2nd.get(botId);
			Integer thirdPlaces = place3rd.get(botId);
			scores.set(i, score.toBuilder()
					.firstPlaces(firstPlaces != null ? firstPlaces : 0)
					.secondPlaces(secondPlaces != null ? secondPlaces : 0)
					.thirdPlaces(thirdPlaces != null ? thirdPlaces : 0)
					.build());
		}
		return scores;
	}

	/**
	 * Calculates 1st, 2nd, and 3rd places.
	 */
	void calculatePlacements() {
		List<Score> scores = getBotScores();
		if (scores.size() >= 1) {
			Score score = scores.get(0);
			Integer count = place1st.get(score.getBotId());
			if (count == null) {
				count = 0;
			}
			place1st.put(score.getBotId(), ++count);
		}
		if (scores.size() >= 2) {
			Score score = scores.get(1);
			Integer count = place2nd.get(score.getBotId());
			if (count == null) {
				count = 0;
			}
			place2nd.put(score.getBotId(), ++count);
		}
		if (scores.size() >= 3) {
			Score score = scores.get(2);
			Integer count = place3rd.get(score.getBotId());
			if (count == null) {
				count = 0;
			}
			place3rd.put(score.getBotId(), ++count);
		}
	}

	/**
	 * Returns the current bot scores ordered with highest total scores first.
	 *
	 * @return a list of bot scores.
	 */
	private List<Score> getBotScores() {
		List<Score> scores = new ArrayList<>();
		botIds.forEach(botId -> scores.add(getScore(botId)));

		scores.sort(Comparator.comparing(Score::getTotalScore).reversed());
		return scores;
	}

	/**
	 * Initializes the map containing the BotRecord record for each bot.
	 */
	private void initializeDamageAndSurvivals() {
		for (int botId : botIds) {
			botRecords.put(botId, new BotRecord());
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

		BotRecord damageRecord = botRecords.get(botId);

		builder.botId(botId);
		builder.survival(RuleConstants.SCORE_PER_SURVIVAL * damageRecord.getSurvivalCount());
		builder.lastSurvivorBonus(RuleConstants.BONUS_PER_LAST_SURVIVOR * damageRecord.getLastSurvivorCount());

		builder.bulletDamage(RuleConstants.SCORE_PER_BULLET_DAMAGE * damageRecord.getTotalBulletDamage());
		builder.ramDamage(RuleConstants.SCORE_PER_RAM_DAMAGE * damageRecord.getTotalRamDamage());

		double bulletKillBonus = 0;
		for (int enemyId : damageRecord.getBulletKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			bulletKillBonus += RuleConstants.BONUS_PER_BULLET_KILL * totalDamage;
		}
		builder.bulletKillBonus(bulletKillBonus);

		double ramKillBonus = 0;
		for (int enemyId : damageRecord.getRamKillEnemyIds()) {
			double totalDamage = damageRecord.getBulletDamage(enemyId) + damageRecord.getRamDamage(enemyId);
			ramKillBonus += RuleConstants.BONUS_PER_RAM_KILL * totalDamage;
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
		BotRecord damageRecord = botRecords.get(botId);

		damageRecord.addBulletDamage(victimBotId, damage);
		if (kill) {
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
		BotRecord damageRecord = botRecords.get(botId);

		damageRecord.addRamDamage(victimBotId, damage);
		if (kill) {
			damageRecord.addRamKillEnemyId(victimBotId);
		}
	}

	/**
	 * Register a bot death.
	 *
	 * @param botId
	 *            is the identifier of the bot that died
	 */
	public void registerBotDeath(int botId) {
		botsAliveIds.remove(botId);

		for (int aliveBotId : botsAliveIds) {
			botRecords.get(aliveBotId).incrementSurvivalCount();
		}

		if (botsAliveIds.size() == 1) {
			int survivorId = botsAliveIds.iterator().next();
			int deadCount = botRecords.size() - botsAliveIds.size();

			botRecords.get(survivorId).addLastSurvivorCount(deadCount);
		}
	}

	/**
	 * Bot record that tracks damage and survival of a bot, and can calculate score.
	 * 
	 * @author Flemming N. Larsen
	 */
	private static class BotRecord {

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
		int getSurvivalCount() {
			return survivalCount;
		}

		/**
		 * Returns the last survivor count, which is the number of bots that was killed, before this bot became the last
		 * survivor.
		 * 
		 * @return the last survivor count.
		 */
		int getLastSurvivorCount() {
			return lastSurvivorCount;
		}

		/**
		 * Returns the total bullet damage dealth by this bot to other bots.
		 * 
		 * @return the total bullet damage.
		 */
		double getTotalBulletDamage() {
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
		double getTotalRamDamage() {
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
		double getBulletDamage(int enemyId) {
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
		double getRamDamage(int enemyId) {
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
		Set<Integer> getBulletKillEnemyIds() {
			return Collections.unmodifiableSet(bulletKillEnemyIds);
		}

		/**
		 * Returns a set of identifiers of all enemy bot that this bot has killed by ramming.
		 * 
		 * @return a set of enemy bot identifiers.
		 */
		Set<Integer> getRamKillEnemyIds() {
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
		void addBulletDamage(int enemyId, double damage) {
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
		void addRamDamage(int enemyId, double damage) {
			double sum = getRamDamage(enemyId) + damage;
			ramDamage.put(enemyId, sum);
		}

		/**
		 * Increment the survival count, meaning that this bot has survived an additional round.
		 */
		void incrementSurvivalCount() {
			survivalCount++;
		}

		/**
		 * Add number of dead enemies to the last survivor count, which only counts, if this bot becomes the last
		 * survivor.
		 *
		 * @param numberOfDeadEnemies
		 *            is the number of dead bots that must be added to the last survivor count.
		 */
		void addLastSurvivorCount(int numberOfDeadEnemies) {
			lastSurvivorCount += numberOfDeadEnemies;
		}

		/**
		 * Adds the identifier of an enemy bot to the set over bots killed by a bullet from this bot.
		 * 
		 * @param enemyId
		 *            is the identifier of the enemy bot that was killed by this bot
		 */
		void addBulletKillEnemyId(int enemyId) {
			bulletKillEnemyIds.add(enemyId);
		}

		/**
		 * Adds the identifier of an enemy bot to the set over bots killed by ramming by this bot.
		 * 
		 * @param enemyId
		 *            is the identifier of the enemy bot that was killed by this bot
		 */
		void addRamKillEnemyId(int enemyId) {
			ramKillEnemyIds.add(enemyId);
		}
	}
}