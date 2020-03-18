package dev.robocode.tankroyale.botapi.events;

/** Event occurring when the bot has hit a wall. */
@SuppressWarnings("unused")
public final class BotHitWallEvent extends Event {

  /** ID of the victim bot that hit the wall. */
  private final int victimId;

  /**
   * Initializes a new instance of the BotHitWallEvent class.
   *
   * @param turnNumber is the turn number when the bot has hit the wall.
   * @param victimId is the ID of the victim bot that hit the wall.
   */
  public BotHitWallEvent(int turnNumber, int victimId) {
    super(turnNumber);
    this.victimId = victimId;
  }
  /**
   * Returns the ID of the victim bot that hit the wall.
   *
   * @return The ID of the victim bot that hit the wall.
   */
  public int getVictimId() {
    return victimId;
  }
}
