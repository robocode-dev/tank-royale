package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.HashMap;
import java.util.Map;

import static dev.robocode.tankroyale.botapi.internal.DefaultEventPriority.*;

/**
 * Manages priorities for bot events in the game.
 * <p>
 * This class maintains a registry of event priorities that determine the order
 * in which events are processed by the bot API.
 * </p>
 */
public final class EventPriorities {
    private static final Map<Class<? extends BotEvent>, Integer> EVENT_PRIORITIES = initializeEventPriorities();

    private EventPriorities() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the default event priorities map.
     *
     * @return A map containing default priority values for all supported event types
     */
    private static Map<Class<? extends BotEvent>, Integer> initializeEventPriorities() {
        Map<Class<? extends BotEvent>, Integer> priorities = new HashMap<>();
        priorities.put(WonRoundEvent.class, WON_ROUND);
        priorities.put(SkippedTurnEvent.class, SKIPPED_TURN);
        priorities.put(TickEvent.class, TICK);
        priorities.put(CustomEvent.class, CUSTOM);
        priorities.put(TeamMessageEvent.class, TEAM_MESSAGE);
        priorities.put(BotDeathEvent.class, BOT_DEATH);
        priorities.put(BulletHitWallEvent.class, BULLET_HIT_WALL);
        priorities.put(BulletHitBulletEvent.class, BULLET_HIT_BULLET);
        priorities.put(BulletHitBotEvent.class, BULLET_HIT_BOT);
        priorities.put(BulletFiredEvent.class, BULLET_FIRED);
        priorities.put(HitByBulletEvent.class, HIT_BY_BULLET);
        priorities.put(HitWallEvent.class, HIT_WALL);
        priorities.put(HitBotEvent.class, HIT_BOT);
        priorities.put(ScannedBotEvent.class, SCANNED_BOT);
        priorities.put(DeathEvent.class, DEATH);
        return priorities;
    }

    /**
     * Sets the priority for a specific event class.
     *
     * @param eventClass The event class to set priority for
     * @param priority The priority value to assign
     * @param <T> Type extending BotEvent
     * @throws NullPointerException if eventClass is null
     */
    public static <T extends BotEvent> void setPriority(Class<T> eventClass, int priority) {
        if (eventClass == null) {
            throw new NullPointerException("Event class cannot be null");
        }
        EVENT_PRIORITIES.put(eventClass, priority);
    }

    /**
     * Gets the priority for a specific event class.
     *
     * @param eventClass The event class to get priority for
     * @param <T> Type extending BotEvent
     * @return The priority value for the specified event class
     * @throws NullPointerException if eventClass is null
     * @throws IllegalStateException if no priority is defined for the event class
     */
    public static <T extends BotEvent> int getPriority(Class<T> eventClass) {
        if (eventClass == null) {
            throw new NullPointerException("Event class cannot be null");
        }
        if (!EVENT_PRIORITIES.containsKey(eventClass)) {
            throw new IllegalStateException("Could not get event priority for the class: " + eventClass.getSimpleName());
        }
        return EVENT_PRIORITIES.get(eventClass);
    }
}