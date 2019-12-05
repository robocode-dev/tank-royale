package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import dev.robocode.tankroyale.botapi.BotResults;

import java.util.List;

/** Event occurring when game has just ended */
@Value
@ToString
public class GameEndedEvent implements IMessage {
  /** Number of rounds played */
  int numberOfRounds;
  /** Results of the battle */
  List<BotResults> results;

  @Builder
  @SuppressWarnings("UnusedDeclaration")
  private GameEndedEvent(int numberOfRounds, List<BotResults> results) {
    this.numberOfRounds = numberOfRounds;
    this.results = results;
  }
}
