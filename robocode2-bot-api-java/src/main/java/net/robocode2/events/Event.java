package net.robocode2.events;

import lombok.ToString;

/** Event occurring during a battle */
@ToString
public abstract class Event {
  /** Current turn number */
  int turnNumber;

  public int getTurnNumber() {
    return turnNumber;
  }
}
