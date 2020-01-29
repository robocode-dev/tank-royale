package dev.robocode.tankroyale.botapi.events;

/** Event occurring during a battle */
public abstract class Event implements IMessage {

  /** Current turn number */
  protected final int turnNumber;

  protected Event(int turnNumber) {
    this.turnNumber = turnNumber;
  }

  /** Returns the current turn number */
  public int getTurnNumber() {
    return turnNumber;
  }
}
