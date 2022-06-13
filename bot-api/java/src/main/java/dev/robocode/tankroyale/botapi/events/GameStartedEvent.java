package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.GameSetup;

/**
 * Event occurring when game has just started.
 */
@SuppressWarnings("unused")
public final class GameStartedEvent implements IEvent {

    // The ID used for identifying your bot in the current battle.
    private final int myId;

    // The game setup for the battle just started.
    private final GameSetup gameSetup;

    /**
     * Initializes a new instance of the GameStartedEvent class.
     *
     * @param myId      is the ID used for identifying your bot in the current battle.
     * @param gameSetup is the game setup for the battle just started.
     */
    public GameStartedEvent(int myId, GameSetup gameSetup) {
        this.myId = myId;
        this.gameSetup = gameSetup;
    }

    /**
     * Returns the ID used for identifying your bot in the current battle.
     *
     * @return The ID used for identifying your bot.
     */
    public int getMyId() {
        return myId;
    }

    /**
     * Returns the game setup for the battle just started.
     *
     * @return The game setup for the battle just started.
     */
    private GameSetup getGameSetup() {
        return gameSetup;
    }
}
