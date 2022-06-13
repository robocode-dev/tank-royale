package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when your bot has died.
 */
@SuppressWarnings("unused")
public final class DeathEvent extends BotEvent {

    /**
     * Initializes a new instance of the DeathEvent class.
     *
     * @param turnNumber is the turn number when your bot died.
     */
    public DeathEvent(int turnNumber) {
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