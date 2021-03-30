package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a new round has just started. */
@SuppressWarnings("unused")
public final class RoundStartedEvent extends BotEvent {

  /** The round number. */
  private final int roundNumber;

  /**
   * Initializes a new instance of the RoundStartedEvent class.
   *
   * @param roundNumber is the round number.
   * @param turnNumber is the turn number.
   */
  public RoundStartedEvent(int roundNumber, int turnNumber) {
    super(turnNumber);
    this.roundNumber = roundNumber;
  }

  /**
   * Returns the round number.
   *
   * @return The round number.
   */
  public int getRoundNumber() {
    return roundNumber;
  }
}
