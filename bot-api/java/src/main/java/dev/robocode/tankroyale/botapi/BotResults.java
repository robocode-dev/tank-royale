package dev.robocode.tankroyale.botapi;

/**
 * Represents individual bot results.
 */
@SuppressWarnings("unused")
public final class BotResults {

    /**
     * Rank/placement of the bot.
     */
    private final int rank;

    /**
     * Survival score.
     */
    private final double survival;

    /**
     * Last survivor score.
     */
    private final double lastSurvivorBonus;

    /**
     * Bullet damage score.
     */
    private final double bulletDamage;

    /**
     * Bullet kill bonus.
     */
    private final double bulletKillBonus;

    /**
     * Ram damage score.
     */
    private final double ramDamage;

    /**
     * Ram kill bonus.
     */
    private final double ramKillBonus;

    /**
     * Total score.
     */
    private final double totalScore;

    /**
     * Number of 1st places.
     */
    private final int firstPlaces;

    /**
     * Number of 2nd places.
     */
    private final int secondPlaces;

    /**
     * Number of 3rd places.
     */
    private final int thirdPlaces;

    /**
     * Initializes a new instance of the BotInfo class.
     *
     * @param rank              is the rank/placement of the bot.
     * @param survival          is the survival score.
     * @param lastSurvivorBonus is the last survivor score.
     * @param bulletDamage      is the bullet damage score.
     * @param bulletKillBonus   is the bullet kill bonus.
     * @param ramDamage         is the ram damage score.
     * @param ramKillBonus      is the ram kill bonus.
     * @param totalScore        is the total score.
     * @param firstPlaces       is the number of 1st places.
     * @param secondPlaces      is the number of 2nd places.
     * @param thirdPlaces       is the number of 3rd places.
     */
    public BotResults(
            int rank,
            double survival,
            double lastSurvivorBonus,
            double bulletDamage,
            double bulletKillBonus,
            double ramDamage,
            double ramKillBonus,
            double totalScore,
            int firstPlaces,
            int secondPlaces,
            int thirdPlaces) {
        this.rank = rank;
        this.survival = survival;
        this.lastSurvivorBonus = lastSurvivorBonus;
        this.bulletDamage = bulletDamage;
        this.bulletKillBonus = bulletKillBonus;
        this.ramDamage = ramDamage;
        this.ramKillBonus = ramKillBonus;
        this.totalScore = totalScore;
        this.firstPlaces = firstPlaces;
        this.secondPlaces = secondPlaces;
        this.thirdPlaces = thirdPlaces;
    }

    /**
     * Returns the rank/placement of the bot, where 1 means 1st place, 4 means 4th place etc.
     *
     * @return The rank of the bot for this battle.
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the accumulated survival score. Every bot still alive score 50 points every time
     * another bot is defeated.
     *
     * @return The survival score.
     */
    public double getSurvival() {
        return survival;
    }

    /**
     * Returns the last survivor score. The last bot alive scores 10 points or each bot that has been
     * defeated.
     *
     * @return The last survivor score.
     */
    public double getLastSurvivorBonus() {
        return lastSurvivorBonus;
    }

    /**
     * Returns the bullet damage score. A bot score 1 point for each point of damage they do to other
     * bots.
     *
     * @return The bullet damage score.
     */
    public double getBulletDamage() {
        return bulletDamage;
    }

    /**
     * Returns the bullet kill-bonus. When a bot kills another bot, it scores an additional 20% points
     * of the total damage it did to that bot.
     *
     * @return The bullet kill-bonus.
     */
    public double getBulletKillBonus() {
        return bulletKillBonus;
    }

    /**
     * Returns the ram damage score. Bots score 2 points for each point of damage inflicted by ramming
     * an enemy bot. Ramming is the act deliberately driving forward (not backward) and hitting
     * another bot.
     *
     * @return The ram damage score.
     */
    public double getRamDamage() {
        return ramDamage;
    }

    /**
     * Returns the ram kill-bonus. When a bot kills another bot due to ramming, it scores an
     * additional 30% of the total ramming damage it did to that bot.
     *
     * @return The ram kill-bonus.
     */
    public double getRamKillBonus() {
        return ramKillBonus;
    }

    /**
     * Returns the total score is the sum of all scores and determines the ranking.
     *
     * @return The total score.
     */
    public double getTotalScore() {
        return totalScore;
    }

    /**
     * Returns the number of 1st places for the bot.
     *
     * @return The number of 1st places.
     */
    public int getFirstPlaces() {
        return firstPlaces;
    }

    /**
     * Returns the number of 2nd places for the bot.
     *
     * @return The number of 2nd places.
     */
    public int getSecondPlaces() {
        return secondPlaces;
    }

    /**
     * Returns the number of 3rd places for the bot.
     *
     * @return The number of 3rd places.
     */
    public int getThirdPlaces() {
        return thirdPlaces;
    }
}
