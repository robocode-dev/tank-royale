package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a new round has just started.
 */
@SuppressWarnings("unused")
public final class RoundStartedEvent implements IEvent {

    // The round number.
    private final int roundNumber;

    /**
     * Initializes a new instance of the RoundStartedEvent class.
     *
     * @param roundNumber is the round number.
     */
    public RoundStartedEvent(int roundNumber) {
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
