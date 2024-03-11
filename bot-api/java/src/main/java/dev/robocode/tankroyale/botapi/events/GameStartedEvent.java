package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.GameSetup;
import dev.robocode.tankroyale.botapi.InitialPosition;

/**
 * Event occurring when game has just started.
 */
@SuppressWarnings("unused")
public final class GameStartedEvent implements IEvent {

    // The id used for identifying your bot in the current battle.
    private final int myId;

    // The initial position of the bot.
    private final InitialPosition initialPosition;

    // The game setup for the battle just started.
    private final GameSetup gameSetup;

    /**
     * Initializes a new instance of the GameStartedEvent class.
     *
     * @param myId            is the id used for identifying your bot in the current battle.
     * @param initialPosition is the initial position of the bot.
     * @param gameSetup       is the game setup for the battle just started.
     */
    public GameStartedEvent(int myId, InitialPosition initialPosition, GameSetup gameSetup) {
        this.myId = myId;
        this.initialPosition = initialPosition;
        this.gameSetup = gameSetup;
    }

    /**
     * Returns the id used for identifying your bot in the current battle.
     *
     * @return The id used for identifying your bot.
     */
    public int getMyId() {
        return myId;
    }

    /**
     * Returns the start position of the bot.
     *
     * @return The start position of the bot.
     */
    public InitialPosition getInitialPosition() {
        return initialPosition;
    }

    /**
     * Returns the game setup for the battle just started.
     *
     * @return The game setup for the battle just started.
     */
    public GameSetup getGameSetup() {
        return gameSetup;
    }
}
