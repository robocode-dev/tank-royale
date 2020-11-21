package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a bot has died. */
@SuppressWarnings("unused")
public final class DeathEvent extends BotEvent {

  /** ID of the bot that has died. */
  private final int victimId;

  /**
   * Initializes a new instance of the BotDeathEvent class.
   *
   * @param turnNumber is the turn number when the bot died.
   * @param victimId is the ID of the bot that has died.
   */
  public DeathEvent(int turnNumber, int victimId) {
    super(turnNumber);
    this.victimId = victimId;
  }

  /**
   * Returns the ID of the bot that has died.
   *
   * @return The ID of the bot that has died.
   */
  public int getVictimId() {
    return victimId;
  }
}
