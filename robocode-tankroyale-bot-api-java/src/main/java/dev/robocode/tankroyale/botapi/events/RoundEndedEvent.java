package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a round has just ended. */
@SuppressWarnings("unused")
public final class RoundEndedEvent extends BotEvent {

  /** The round number. */
  private final int roundNumber;

  /**
   * Initializes a new instance of the RoundEndedEvent class.
   *
   * @param roundNumber is the round number.
   * @param turnNumber is the turn number.
   */
  public RoundEndedEvent(int roundNumber, int turnNumber) {
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
