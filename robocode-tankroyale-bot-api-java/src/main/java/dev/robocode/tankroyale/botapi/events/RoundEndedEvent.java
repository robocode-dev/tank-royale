package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a round has just ended. */
@SuppressWarnings("unused")
public final class RoundEndedEvent implements IEvent {

  /** The round number. */
  private final int roundNumber;

  /**
   * Initializes a new instance of the RoundEndedEvent class.
   *
   * @param roundNumber is the round number.
   */
  public RoundEndedEvent(int roundNumber) {
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
