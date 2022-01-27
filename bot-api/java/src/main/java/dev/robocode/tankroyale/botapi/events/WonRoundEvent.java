package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a bot has won the round.
 */
public final class WonRoundEvent extends BotEvent {

    /**
     * Initializes a new instance of the WonRoundEvent class.
     *
     * @param turnNumber is the turn number when the bot won the round.
     */
    public WonRoundEvent(int turnNumber) {
        super(turnNumber);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This event is critical.
     *
     * @return true
     */
    @Override
    public boolean isCritical() {
        return true;
    }
}
