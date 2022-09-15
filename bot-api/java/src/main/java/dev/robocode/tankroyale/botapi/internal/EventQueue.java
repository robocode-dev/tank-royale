package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Integer.MIN_VALUE;

final class EventQueue {

    private static final int MAX_QUEUE_SIZE = 256;
    private static final int MAX_EVENT_AGE = 2;

    private final BaseBotInternals baseBotInternals;
    private final BotEventHandlers botEventHandlers;

    private final List<BotEvent> events = new CopyOnWriteArrayList<>();

    private BotEvent currentTopEvent;
    private int currentTopEventPriority;

    private final Set<Class<? extends BotEvent>> interruptibles = new HashSet<>();

    public EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers) {
        this.baseBotInternals = baseBotInternals;
        this.botEventHandlers = botEventHandlers;
    }

    void clear() {
        events.clear();
        baseBotInternals.getConditions().clear(); // conditions might be added in the bots run() method each round
        currentTopEvent = null;
        currentTopEventPriority = MIN_VALUE;
    }

    List<BotEvent> getEvents() {
        return new ArrayList<>(events);
    }

    void clearEvents() {
        events.clear();
    }

    void setInterruptible(boolean interruptible) {
        setInterruptible(currentTopEvent.getClass(), interruptible);
    }

    void setInterruptible(Class<? extends BotEvent> eventClass, boolean interruptible) {
        if (interruptible) {
            interruptibles.add(eventClass);
        } else {
            interruptibles.remove(eventClass);
        }
    }

    private boolean isInterruptible() {
        return interruptibles.contains(currentTopEvent.getClass());
    }

    void addEventsFromTick(TickEvent event) {
        addEvent(event);
        event.getEvents().forEach(this::addEvent);

        addCustomEvents();
    }

    void dispatchEvents(int currentTurn) {
        removeOldEvents(currentTurn);

        sortEvents();

        while (baseBotInternals.isRunning() && events.size() > 0) {
            var event = events.get(0);
            var priority = getPriority(event);

            try {
                if (priority < currentTopEventPriority) {
                    return; // Exit when event priority is lower than the current event being processed
                }
                // Same event?
                if (priority == currentTopEventPriority) {
                    if (!isInterruptible()) {
                        // Ignore same event occurring again, when not interruptible
                        return;
                    }
                    setInterruptible(event.getClass(), false);
                    // The current event handler must be interrupted (by throwing an InterruptEventHandlerException)
                    throw new InterruptEventHandlerException();
                }
            } finally {
                events.remove(event);
            }

            int oldTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = priority;
            currentTopEvent = event;

            try {
                if (isNotOldOrCriticalEvent(event, currentTurn)) {
                    botEventHandlers.fire(event);
                }
                setInterruptible(event.getClass(), false);

            } catch (InterruptEventHandlerException ignore) {
                // Expected when event handler is being interrupted
            } finally {
                currentTopEventPriority = oldTopEventPriority;
            }
        }
    }

    private void removeOldEvents(int currentTurn) {
        events.removeIf(event -> isOldAndNonCriticalEvent(event, currentTurn));
    }

    private void sortEvents() {
        events.sort((e1, e2) -> {
            // Higher priority gives negative delta -> becomes first
            int diff = (e2.isCritical() ? 1 : 0) - (e1.isCritical() ? 1 : 0);
            if (diff != 0) {
                return diff;
            }
            // Lower (older) turn number gives negative delta -> becomes first
            diff = e1.getTurnNumber() - e2.getTurnNumber();
            if (diff != 0) {
                return diff;
            }
            // Higher priority gives negative delta -> becomes first
            return getPriority(e2) - getPriority(e1);
        });
    }

    private int getPriority(BotEvent event) {
        @SuppressWarnings("unchecked")
        int priority = baseBotInternals.getPriority((Class<BotEvent>) event.getClass());
        return priority;
    }

    private static boolean isNotOldOrCriticalEvent(BotEvent event, int currentTurn) {
        var isNotOld = event.getTurnNumber() + MAX_EVENT_AGE >= currentTurn;
        var isCritical = event.isCritical();
        return isNotOld || isCritical;
    }

    private static boolean isOldAndNonCriticalEvent(BotEvent event, int currentTurn) {
        var isOld = event.getTurnNumber() + MAX_EVENT_AGE < currentTurn;
        var isNonCritical = !event.isCritical();
        return isOld && isNonCritical;
    }

    private void addEvent(BotEvent event) {
        if (events.size() > MAX_QUEUE_SIZE) {
            System.err.println("Maximum event queue size has been reached: " + MAX_QUEUE_SIZE);
        } else {
            events.add(event);
        }
    }

    private void addCustomEvents() {
        baseBotInternals.getConditions().stream().filter(Condition::test).forEach(condition ->
                addEvent(new CustomEvent(baseBotInternals.getCurrentTick().getTurnNumber(), condition))
        );
    }
}