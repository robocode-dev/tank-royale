package net.robocode2.events;

/** Event occurring during a battle */
public abstract class Event {
  /** Current turn number */
  int turnNumber;

  public int getTurnNumber() {
    return turnNumber;
  }
}
