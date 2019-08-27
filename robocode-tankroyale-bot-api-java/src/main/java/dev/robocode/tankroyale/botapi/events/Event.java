package dev.robocode.tankroyale.botapi.events;

import lombok.ToString;

/** Event occurring during a battle */
@ToString
public abstract class Event implements Message {
  /** Current turn number */
  int turnNumber;

  public int getTurnNumber() {
    return turnNumber;
  }
}
