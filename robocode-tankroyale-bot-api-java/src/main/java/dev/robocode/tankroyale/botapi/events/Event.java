package dev.robocode.tankroyale.botapi.events;

/** Event occurring during a battle. */
public abstract class Event implements IMessage {

  /** Turn number when the event occurred. */
  protected final int turnNumber;

  /**
   * Initializes a new instance of the Event class.
   *
   * @param turnNumber is the turn number when the event occurred.
   */
  protected Event(int turnNumber) {
    this.turnNumber = turnNumber;
  }

  /**
   * Returns the turn number when the event occurred.
   *
   * @return The turn number when the event occurred.
   */
  public int getTurnNumber() {
    return turnNumber;
  }
}
