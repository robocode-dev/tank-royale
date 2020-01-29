package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
 * for a specific turn
 */
public final class SkippedTurnEvent extends Event {

  public SkippedTurnEvent(int turnNumber) {
    super(turnNumber);
  }
}
