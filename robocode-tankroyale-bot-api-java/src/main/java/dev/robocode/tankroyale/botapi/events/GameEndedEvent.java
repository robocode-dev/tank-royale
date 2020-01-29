package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BotResults;

import java.util.List;

/** Event occurring when game has just ended */
@SuppressWarnings("unused")
public final class GameEndedEvent implements IMessage {

  /** Number of rounds played */
  private final int numberOfRounds;

  /** Results of the battle */
  private final List<BotResults> results;

  public GameEndedEvent(int numberOfRounds, List<BotResults> results) {
    this.numberOfRounds = numberOfRounds;
    this.results = results;
  }

  /** Returns the number of rounds played */
  public int getNumberOfRounds() {
    return numberOfRounds;
  }

  /** Returns the results of the battle */
  public List<BotResults> getResults() {
    return results;
  }
}
