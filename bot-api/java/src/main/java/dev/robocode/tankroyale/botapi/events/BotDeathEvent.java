package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when another bot has died.
 */
@SuppressWarnings("unused")
public final class BotDeathEvent extends BotEvent {

    // id of the bot that has died.
    private final int victimId;

    /**
     * Initializes a new instance of the BotDeathEvent class.
     *
     * @param turnNumber is the turn number when the bot died.
     * @param victimId   is the id of the bot that has died.
     */
    public BotDeathEvent(int turnNumber, int victimId) {
        super(turnNumber);
        this.victimId = victimId;
    }

    /**
     * Returns the id of the bot that has died.
     *
     * @return The id of the bot that has died.
     */
    public int getVictimId() {
        return victimId;
    }
}