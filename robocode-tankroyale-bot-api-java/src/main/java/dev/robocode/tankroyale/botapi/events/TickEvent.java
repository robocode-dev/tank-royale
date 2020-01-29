package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BotState;
import dev.robocode.tankroyale.botapi.BulletState;

import java.util.Collection;

/** Event occurring whenever a new turn in a round has started */
public final class TickEvent extends Event {

  /** Current round number */
  private final int roundNumber;

  /** Current state of this bot */
  private final BotState botState;

  /** Current state of the bullets fired by this bot */
  private final Collection<BulletState> bulletStates;

  /** Events occurring in the turn relevant for this bot */
  private final Collection<? extends Event> events;

  public TickEvent(
      int turnNumber,
      int roundNumber,
      BotState botState,
      Collection<BulletState> bulletStates,
      Collection<? extends Event> events) {
    super(turnNumber);
    this.roundNumber = roundNumber;
    this.botState = botState;
    this.bulletStates = bulletStates;
    this.events = events;
  }

  /** Returns the current round number */
  public int getRoundNumber() {
    return roundNumber;
  }

  /** Returns the current state of this bot */
  public BotState getBotState() {
    return botState;
  }

  /** Returns the Current state of the bullets fired by this bot */
  public Collection<BulletState> getBulletStates() {
    return bulletStates;
  }

  /** Returns the events occurring in the turn relevant for this bot */
  public Collection<? extends Event> getEvents() {
    return events;
  }
}
