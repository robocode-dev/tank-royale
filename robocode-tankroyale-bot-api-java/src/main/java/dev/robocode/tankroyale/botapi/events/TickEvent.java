package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BotState;
import dev.robocode.tankroyale.botapi.BulletState;

import java.util.Collection;

/** Event occurring whenever a new turn in a round has started. */
public final class TickEvent extends Event {

  /** Current round number. */
  private final int roundNumber;

  /** Enemy count. */
  private final int enemyCount;

  /** Current state of this bot. */
  private final BotState botState;

  /** Current state of the bullets fired by this bot. */
  private final Collection<BulletState> bulletStates;

  /** Events occurring in the turn relevant for this bot. */
  private final Collection<? extends Event> events;

  /**
   * Initializes a new instance of the TickEvent class.
   *
   * @param turnNumber is the current turn number in the battle.
   * @param roundNumber is the current round number in the battle.
   * @param enemyCount is the number of enemies left in the round.
   * @param botState is the current state of this bot.
   * @param bulletStates is the current state of the bullets fired by this bot.
   * @param events is the events occurring in the turn relevant for this bot.
   */
  public TickEvent(
      int turnNumber,
      int roundNumber,
      int enemyCount,
      BotState botState,
      Collection<BulletState> bulletStates,
      Collection<? extends Event> events) {
    super(turnNumber);
    this.roundNumber = roundNumber;
    this.enemyCount = enemyCount;
    this.botState = botState;
    this.bulletStates = bulletStates;
    this.events = events;
  }

  /**
   * Returns the current round number.
   *
   * @return The current round number.
   */
  public int getRoundNumber() {
    return roundNumber;
  }

  /**
   * Returns the enemy count.
   *
   * @return The enemy count.
   */
  public int getEnemyCount() {
    return enemyCount;
  }

  /**
   * Returns the current state of this bot.
   *
   * @return The current state of this bot.
   */
  public BotState getBotState() {
    return botState;
  }

  /**
   * Returns the Current state of the bullets fired by this bot.
   *
   * @return The Current state of the bullets fired by this bot.
   */
  public Collection<BulletState> getBulletStates() {
    return bulletStates;
  }

  /**
   * Returns the events occurring in the turn relevant for this bot.
   *
   * @return The events occurring in the turn relevant for this bot.
   */
  public Collection<? extends Event> getEvents() {
    return events;
  }
}
