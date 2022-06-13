package dev.robocode.tankroyale.botapi.events;

/**
 * A custom event occurring when a condition has been met.
 */
public final class CustomEvent extends BotEvent {

    // Condition that was met to trigger this custom event.
    private final Condition condition;

    /**
     * Initializes a new instance of the CustomEvent class.
     *
     * @param turnNumber is the turn number when the condition was met.
     * @param condition  is the condition that has been met.
     */
    public CustomEvent(int turnNumber, Condition condition) {
        super(turnNumber);
        this.condition = condition;
    }

    /**
     * Returns the condition that was met to trigger this custom event.
     *
     * @return The condition that was met to trigger this custom event.
     */
    public Condition getCondition() {
        return condition;
    }
}
