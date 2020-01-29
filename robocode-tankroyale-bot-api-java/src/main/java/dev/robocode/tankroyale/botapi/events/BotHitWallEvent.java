package dev.robocode.tankroyale.botapi.events;

/** Event occurring when the bot has hit a wall */
@SuppressWarnings("unused")
public final class BotHitWallEvent extends Event {

  /** ID of the victim bot that hit the wall */
  private final int victimId;

  public BotHitWallEvent(int turnNumber, int victimId) {
    super(turnNumber);
    this.victimId = victimId;
  }
  /** Returns the ID of the victim bot that hit the wall */
  public int getVictimId() {
    return victimId;
  }
}
