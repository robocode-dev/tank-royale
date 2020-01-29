package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a bot has died */
@SuppressWarnings("unused")
public final class BotDeathEvent extends Event {

  /** ID of the bot that has died */
  private final int victimId;

  public BotDeathEvent(int turnNumber, int victimId) {
    super(turnNumber);
    this.victimId = victimId;
  }

  /** Returns the ID of the bot that has died */
  public int getVictimId() {
    return victimId;
  }
}
