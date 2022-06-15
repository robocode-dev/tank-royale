package dev.robocode.tankroyale.botapi.events;

/**
 * Bot event occurring during a battle.
 */
public abstract class BotEvent implements IEvent {

    // Turn number when the event occurred.
    private final int turnNumber;

    /**
     * Initializes a new instance of the Event class.
     *
     * @param turnNumber is the turn number when the event occurred.
     */
    protected BotEvent(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    /**
     * Returns the turn number when the event occurred.
     *
     * @return The turn number when the event occurred.
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * Indicates if this event is critical, and hence should not be removed from event queue when it gets old.
     *
     * @return true if this event is critical; false otherwise. Default is false.
     */
    public boolean isCritical() {
        return false;
    }
}