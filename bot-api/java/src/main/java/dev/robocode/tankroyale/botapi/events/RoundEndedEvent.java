package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BotResults;

/**
 * Event occurring when a round has just ended.
 */
@SuppressWarnings("unused")
public final class RoundEndedEvent implements IEvent {

    // The round number.
    private final int roundNumber;

    // The turn number.
    private final int turnNumber;

    // The accumulated bot results.
    private final BotResults results;

    /**
     * Initializes a new instance of the RoundEndedEvent class.
     *
     * @param roundNumber is the round number.
     * @param turnNumber  is the turn number.
     * @param results     is the accumulated bot results at the end of the round.
     */
    public RoundEndedEvent(int roundNumber, int turnNumber, BotResults results) {
        this.roundNumber = roundNumber;
        this.turnNumber = turnNumber;
        this.results = results;
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

    /**
     * Returns the accumulated bot results at the end of the round.
     *
     * @return The accumulated bot results at the end of the round.
     */
    public BotResults getResults() {
        return results;
    }
}
