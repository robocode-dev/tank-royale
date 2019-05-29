package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.robocode2.BotState;
import net.robocode2.BulletState;

import java.util.List;

/** Event occurring whenever a new turn in a round has started */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TickEvent extends Event {
  /** Current round number */
  int roundNumber;
  /** Current state of this bot */
  BotState botState;
  /** Current state of the bullets fired by this bot */
  List<BulletState> bulletStates;
  /** Events occurring in the turn relevant for this bot */
  List<Event> events;

  @Builder
  private TickEvent(
      int turnNumber,
      int roundNumber,
      BotState botState,
      List<BulletState> bulletStates,
      List<Event> events) {
    this.turnNumber = turnNumber;
    this.roundNumber = roundNumber;
    this.botState = botState;
    this.bulletStates = bulletStates;
    this.events = events;
  }
}
