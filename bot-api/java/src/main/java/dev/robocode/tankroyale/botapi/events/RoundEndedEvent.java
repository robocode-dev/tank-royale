package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a round has just ended.
 */
@SuppressWarnings("unused")
public final class RoundEndedEvent implements IEvent {

    /**
     * The round number.
     */
    private final int roundNumber;

    /**
     * The turn number.
     */
    private final int turnNumber;

    /**
     * Initializes a new instance of the RoundEndedEvent class.
     *
     * @param roundNumber is the round number.
     * @param turnNumber  is the turn number.
     */
    public RoundEndedEvent(int roundNumber, int turnNumber) {
        this.roundNumber = roundNumber;
        this.turnNumber = turnNumber;
    }

    /**
     * Returns the round number.
     *
     * @return The round number.
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Returns the turn number.
     *
     * @return The turn number.
     */
    public int getTurnNumber() {
        return turnNumber;
    }
}
