package dev.robocode.tankroyale.botapi;

/** Game setup retrieved when game is started. */
@SuppressWarnings("unused")
public final class GameSetup {

  /** Game type, e.g. "melee" */
  private final String gameType;

  /** Width of the arena measured in pixels */
  private final int arenaWidth;

  /** Height of the arena measured in pixels */
  private final int arenaHeight;

  /** Number of rounds in a battle */
  private final int numberOfRounds;

  /**
   * Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun is able to
   * fire.
   */
  private final double gunCoolingRate;

  /**
   * Maximum number of inactive turns allowed, where a bot does not take any action before it is
   * zapped by the game
   */
  private final int maxInactivityTurns;

  /** Timeout in milliseconds for sending intent after having received 'tick' message */
  private final int turnTimeout;

  /**
   * Time limit in milliseconds for sending ready message after having received 'new battle' message
   */
  private final int readyTimeout;

  public GameSetup(
      String gameType,
      int arenaWidth,
      int arenaHeight,
      int numberOfRounds,
      double gunCoolingRate,
      int maxInactivityTurns,
      int turnTimeout,
      int readyTimeout) {
    this.gameType = gameType;
    this.arenaWidth = arenaWidth;
    this.arenaHeight = arenaHeight;
    this.numberOfRounds = numberOfRounds;
    this.gunCoolingRate = gunCoolingRate;
    this.maxInactivityTurns = maxInactivityTurns;
    this.turnTimeout = turnTimeout;
    this.readyTimeout = readyTimeout;
  }

  /** Returns the game type, e.g. "melee" */
  public String getGameType() {
    return gameType;
  }

  /** Returns the width of the arena measured in pixels */
  public int getArenaWidth() {
    return arenaWidth;
  }

  /** Returns the height of the arena measured in pixels */
  public int getArenaHeight() {
    return arenaHeight;
  }

  /** Returns the number of rounds in a battle */
  public int getNumberOfRounds() {
    return numberOfRounds;
  }

  /**
   * Returns the gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun
   * is able to fire.
   */
  public double getGunCoolingRate() {
    return gunCoolingRate;
  }

  /**
   * Returns the Maximum number of inactive turns allowed, where a bot does not take any action
   * before it is zapped by the game
   */
  public int getMaxInactivityTurns() {
    return maxInactivityTurns;
  }

  /** Returns the timeout in milliseconds for sending intent after having received 'tick' message */
  public int getTurnTimeout() {
    return turnTimeout;
  }

  /**
   * Returns the time limit in milliseconds for sending ready message after having received 'new
   * battle' message
   */
  public int getReadyTimeout() {
    return readyTimeout;
  }
}
