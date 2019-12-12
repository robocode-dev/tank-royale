package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BotState;
import dev.robocode.tankroyale.botapi.BulletState;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Collection;

/** Event occurring whenever a new turn in a round has started */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class TickEvent extends Event {
  /** Current round number */
  int roundNumber;
  /** Current state of this bot */
  BotState botState;
  /** Current state of the bullets fired by this bot */
  Collection<BulletState> bulletStates;
  /** Events occurring in the turn relevant for this bot */
  Collection<? extends Event> events;

  @Builder
  @SuppressWarnings("unused")
  private TickEvent(
      int turnNumber,
      int roundNumber,
      BotState botState,
      Collection<BulletState> bulletStates,
      Collection<? extends Event> events) {
    this.turnNumber = turnNumber;
    this.roundNumber = roundNumber;
    this.botState = botState;
    this.bulletStates = bulletStates;
    this.events = events;
  }
}
