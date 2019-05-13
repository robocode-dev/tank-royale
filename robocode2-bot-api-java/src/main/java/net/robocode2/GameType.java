package net.robocode2;

/** Predefined (known) game types. */
public enum GameType {
  /** Melee with every bot against every other bot */
  MELEE("melee"),
  /** One versus one (1-vs-1) */
  ONE_VS_ONE("1v1"),
  /** Twin dual with two team both containing two bots */
  TWIN_DUAL("twin dual");

  private String gameType;

  GameType(String gameType) {
    this.gameType = gameType;
  }

  @Override
  public String toString() {
    return gameType;
  }
}
