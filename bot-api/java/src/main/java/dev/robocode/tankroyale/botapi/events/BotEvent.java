package dev.robocode.tankroyale.botapi.events;

import java.util.HashMap;
import java.util.Map;

import static dev.robocode.tankroyale.botapi.events.DefaultEventPriority.*;

/**
 * Bot event occurring during a battle.
 */
public abstract class BotEvent implements IEvent {

    private static final Map<Class<? extends BotEvent>, Integer> eventPriorities = new HashMap<>();

    static {
        eventPriorities.put(TickEvent.class, ON_TICK);
        eventPriorities.put(WonRoundEvent.class, ON_WON_ROUND);
        eventPriorities.put(SkippedTurnEvent.class, ON_SKIPPED_TURN);
        eventPriorities.put(CustomEvent.class, ON_CONDITION);
        eventPriorities.put(BotDeathEvent.class, ON_BOT_DEATH);
        eventPriorities.put(BulletFiredEvent.class, ON_BULLET_FIRED);
        eventPriorities.put(BulletHitWallEvent.class, ON_BULLET_HIT_WALL);
        eventPriorities.put(BulletHitBulletEvent.class, ON_BULLET_HIT_BULLET);
        eventPriorities.put(BulletHitBotEvent.class, ON_BULLET_HIT);
        eventPriorities.put(HitByBulletEvent.class, ON_HIT_BY_BULLET);
        eventPriorities.put(HitWallEvent.class, ON_HIT_WALL);
        eventPriorities.put(HitBotEvent.class, ON_HIT_BOT);
        eventPriorities.put(ScannedBotEvent.class, ON_SCANNED_BOT);
        eventPriorities.put(DeathEvent.class, ON_DEATH);
    }

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
     * Returns the event priority.
     *
     * @return The event priority.
     * @see #setPriority(int)
     */
    public int getPriority() {
        var clazz = getClass();
        if (!eventPriorities.containsKey(clazz)) {
            throw new IllegalStateException("Could not get event priority for the class: " + clazz.getSimpleName());
        }
        return eventPriorities.get(getClass());
    }

    /**
     * Changes the event priority for the event class which determines which event types (classes) that must be handled
     * before others. Events with higher priorities will be handled before events with lower priorities.
     *
     * <p>Note that you should normally not need to change the event priority
     *
     * @param priority is the new priority. Typically, a positive number from 1 to 150. The greater value, the higher
     *                 priority.
     * @see #getPriority()
     */
    public void setPriority(int priority) {
        eventPriorities.put(getClass(), priority);
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