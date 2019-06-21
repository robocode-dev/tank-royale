package net.robocode2;

/** Predefined (known) game types. */
public final class GameType {
  private GameType() {}

  /** Melee with every bot against every other bot */
  public final static String MELEE = "melee";

  /** One versus one (1-vs-1) */
  public final static String ONE_VS_ONE ="1v1";

  /** Twin dual with two team both containing two bots */
  public final static String TWIN_DUAL = "twin dual";
}
