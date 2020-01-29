package dev.robocode.tankroyale.botapi;

/** Individual bot results */
@SuppressWarnings("unused")
public final class BotResults {

  /** Identifier of the bot used in this battle */
  private final int id;

  /** Rank/placement of the bot, where 1 is 1st place, 4 is 4th place etc. */
  private final int rank;

  /** Survival score gained whenever another bot is defeated */
  private final double survival;

  /** Last survivor score as last survivor in a round */
  private final double lastSurvivorBonus;

  /** Bullet damage given */
  private final double bulletDamage;

  /** Bullet kill bonus */
  private final double bulletKillBonus;

  /** Ram damage given */
  private final double ramDamage;

  /** Ram kill bonus */
  private final double ramKillBonus;

  /** Total score */
  private final double totalScore;

  /** Number of 1st places */
  private final int firstPlaces;

  /** Number of 2nd places */
  private final int secondPlaces;

  /** Number of 3rd places */
  private final int thirdPlaces;

  public BotResults(
      int id,
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
    this.id = id;
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

  /** Returns the identifier of the bot used in this battle */
  public int getId() {
    return id;
  }

  /** Returns the rank/placement of the bot, where 1 is 1st place, 4 is 4th place etc. */
  public int getRank() {
    return rank;
  }

  /** Returns the survival score gained whenever another bot is defeated */
  public double getSurvival() {
    return survival;
  }

  /** Returns the last survivor score as last survivor in a round */
  public double getLastSurvivorBonus() {
    return lastSurvivorBonus;
  }

  /** Returns the bullet damage given */
  public double getBulletDamage() {
    return bulletDamage;
  }

  /** Returns the bullet kill bonus */
  public double getBulletKillBonus() {
    return bulletKillBonus;
  }

  /** Returns the ram damage given */
  public double getRamDamage() {
    return ramDamage;
  }

  /** Returns the ram kill bonus */
  public double getRamKillBonus() {
    return ramKillBonus;
  }

  /** Returns the total score */
  public double getTotalScore() {
    return totalScore;
  }

  /** Returns the number of 1st places */
  public int getFirstPlaces() {
    return firstPlaces;
  }

  /** Returns the number of 2nd places */
  public int getSecondPlaces() {
    return secondPlaces;
  }

  /** Returns the number of 3rd places */
  public int getThirdPlaces() {
    return thirdPlaces;
  }
}
