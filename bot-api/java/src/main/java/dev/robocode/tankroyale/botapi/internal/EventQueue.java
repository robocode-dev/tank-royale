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
        clearEvents();
        baseBotInternals.getConditions().clear(); // conditions might be added in the bots run() method each round
        currentTopEvent = null;
        currentTopEventPriority = MIN_VALUE;
    }

    List<BotEvent> getEvents() {
        return new ArrayList<>(events);
    }

    void clearEvents() {
        synchronized (events) {
            events.clear();
        }
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

    void dispatchEvents(int turnNumber) {
        removeOldEvents(turnNumber);
        sortEvents();

        while (isBotRunning()) {
            BotEvent botEvent = getNextEvent();
            if (botEvent == null || isSameEvent(botEvent)) {
                break;
            }

            int priority = getPriority(botEvent);
            int originalTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = priority;
            currentTopEvent = botEvent;

            try {
                handleEvent(botEvent, turnNumber);
            } catch (InterruptEventHandlerException ignore) {
                // Expected when event handler is being interrupted
            } finally {
                currentTopEventPriority = originalTopEventPriority;
            }
        }
    }

    private void removeOldEvents(int turnNumber) {
        synchronized (events) {
            events.removeIf(event -> isOldAndNonCriticalEvent(event, turnNumber));
        }
    }

    private void sortEvents() {
        events.sort((botEvent1, botEvent2) -> {
            // Critical must be placed before non-critical
            int diff = (botEvent2.isCritical() ? 1 : 0) - (botEvent1.isCritical() ? 1 : 0);
            if (diff != 0) {
                return diff;
            }
            // Lower (older) turn number must be placed before higher (newer) turn number
            diff = botEvent1.getTurnNumber() - botEvent2.getTurnNumber();
            if (diff != 0) {
                return diff;
            }
            // Higher priority value must be placed before lower priority value
            return getPriority(botEvent2) - getPriority(botEvent1);
        });
    }

    private boolean isBotRunning() {
        return baseBotInternals.isRunning();
    }

    private BotEvent getNextEvent() {
        synchronized (events) {
            return events.isEmpty() ? null : events.remove(0);
        }
    }

    private boolean isSameEvent(BotEvent botEvent) {
        return getPriority(botEvent) == currentTopEventPriority &&
                (currentTopEventPriority > MIN_VALUE && isInterruptible());
    }

    private int getPriority(BotEvent botEvent) {
        @SuppressWarnings("unchecked")
        var eventClass = (Class<BotEvent>) botEvent.getClass();
        return baseBotInternals.getPriority(eventClass);
    }

    private void handleEvent(BotEvent botEvent, int turnNumber) {
        if (isNotOldOrIsCriticalEvent(botEvent, turnNumber)) {
            botEventHandlers.fire(botEvent);
        }
        var isInterruptible = isInterruptible();

        setInterruptible(botEvent.getClass(), false); // clear interruptible flag

        if (isInterruptible) {
            throw new InterruptEventHandlerException();
        }
    }

    private static boolean isNotOldOrIsCriticalEvent(BotEvent botEvent, int turnNumber) {
        var isNotOld = botEvent.getTurnNumber() + MAX_EVENT_AGE >= turnNumber;
        var isCritical = botEvent.isCritical();
        return isNotOld || isCritical;
    }

    private static boolean isOldAndNonCriticalEvent(BotEvent botEvent, int turnNumber) {
        var isOld = botEvent.getTurnNumber() + MAX_EVENT_AGE < turnNumber;
        var isNonCritical = !botEvent.isCritical();
        return isOld && isNonCritical;
    }

    private void addEvent(BotEvent botEvent) {
        synchronized (events) {
            if (events.size() > MAX_QUEUE_SIZE) {
                System.err.println("Maximum event queue size has been reached: " + MAX_QUEUE_SIZE);
            } else {
                events.add(botEvent);
            }
        }
    }

    private void addCustomEvents() {
        baseBotInternals.getConditions().stream().filter(Condition::test).forEach(condition ->
                addEvent(new CustomEvent(baseBotInternals.getCurrentTickOrThrow().getTurnNumber(), condition))
        );
    }
}