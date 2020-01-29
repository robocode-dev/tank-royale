package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a bot has won the round */
public final class WonRoundEvent extends Event {

  public WonRoundEvent(int turnNumber) {
    super(turnNumber);
  }
}
