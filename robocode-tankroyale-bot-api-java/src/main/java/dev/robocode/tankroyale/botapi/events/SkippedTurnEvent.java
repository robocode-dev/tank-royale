package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
 * for a specific turn.
 */
public final class SkippedTurnEvent extends BotEvent {

  /**
   * Initializes a new instance of the SkippedTurnEvent class.
   *
   * @param turnNumber is the turn number which was skipped.
   */
  public SkippedTurnEvent(int turnNumber) {
    super(turnNumber);
  }
}
