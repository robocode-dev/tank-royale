package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BotResults;

/**
 * Event occurring when game has just ended.
 */
@SuppressWarnings("unused")
public final class GameEndedEvent implements IEvent {

    // Number of rounds played.
    private final int numberOfRounds;

    // Results of the battle.
    private final BotResults results;

    /**
     * Initializes a new instance of the GameEndedEvent class.
     *
     * @param numberOfRounds is the number of rounds played.
     * @param results        is the bot results of the battle.
     */
    public GameEndedEvent(int numberOfRounds, BotResults results) {
        this.numberOfRounds = numberOfRounds;
        this.results = results;
    }

    /**
     * Returns the number of rounds played.
     *
     * @return The number of rounds played.
     */
    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    /**
     * Returns the results of the battle.
     *
     * @return The results of the battle.
     */
    public BotResults getResults() {
        return results;
    }
}
