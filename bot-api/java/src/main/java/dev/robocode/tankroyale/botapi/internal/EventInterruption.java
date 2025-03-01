package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.events.BotEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages event interruption settings for bot events.
 * <p>
 * This class is responsible for tracking which bot event types are marked as interruptible.
 * Events that are marked as interruptible can interrupt the normal execution event flow of the bot.
 * </p>
 */
public class EventInterruption {

    /** Set containing all event classes that are currently marked as interruptible. */
    private static final Set<Class<? extends BotEvent>> interruptibles = new HashSet<>();

    /**
     * Sets whether a specific event class should be interruptible or not.
     *
     * @param eventClass The class of the event to configure
     * @param interruptible {@code true} if the event should be interruptible; {@code false} otherwise
     */
    public static void setInterruptible(Class<? extends BotEvent> eventClass, boolean interruptible) {
        if (interruptible) {
            interruptibles.add(eventClass);
        } else {
            interruptibles.remove(eventClass);
        }
    }

    /**
     * Checks if a specific event class is marked as interruptible.
     *
     * @param eventClass The class of the event to check
     * @return {@code true} if the event is interruptible; {@code false} otherwise
     */
    public static boolean isInterruptible(Class<? extends BotEvent> eventClass) {
        return interruptibles.contains(eventClass);
    }
}