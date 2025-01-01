package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.*;

import static java.lang.Integer.MIN_VALUE;

final class EventQueue {

    private static final int MAX_QUEUE_SIZE = 256;
    private static final int MAX_EVENT_AGE = 2;

    private final BaseBotInternals baseBotInternals;
    private final BotEventHandlers botEventHandlers;

    private final List<BotEvent> events = Collections.synchronizedList(new ArrayList<>());

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
        currentTopEventPriority = MIN_VALUE;
    }

    List<BotEvent> getEvents(int turnNumber) {
        removeOldEvents(turnNumber);
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
//        dumpEvents(); // for debugging purposes

        removeOldEvents(turnNumber);
        sortEvents();

        while (isBotRunning()) {
            BotEvent currentEvent = getNextEvent();
            if (currentEvent == null) {
                break;
            }
            if (isSameEvent(currentEvent)) {
                if (isInterruptible()) {
                    setInterruptible(currentEvent.getClass(), false); // clear interruptible flag

                    // We are already in an event handler, took action, and a new event was generated.
                    // So we want to break out of the old handler to process the new event here.
                    throw new ThreadInterruptedException();
                }
                break;
            }

            int oldTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = getPriority(currentEvent);
            currentTopEvent = currentEvent;

            events.remove(currentEvent);

            try {
                handleEvent(currentEvent, turnNumber);
            } catch (ThreadInterruptedException ignore) {
                // Expected when event handler is interrupted on purpose
            } finally {
                currentTopEventPriority = oldTopEventPriority;
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
        return getPriority(botEvent) == currentTopEventPriority;
    }

    private int getPriority(BotEvent botEvent) {
        @SuppressWarnings("unchecked")
        var eventClass = (Class<BotEvent>) botEvent.getClass();
        return baseBotInternals.getPriority(eventClass);
    }

    private void handleEvent(BotEvent botEvent, int turnNumber) {
        try {
            if (isNotOldOrIsCriticalEvent(botEvent, turnNumber)) {
                botEventHandlers.fireEvent(botEvent);
            }
        } finally {
            setInterruptible(botEvent.getClass(), false);
        }
    }

    private static boolean isNotOldOrIsCriticalEvent(BotEvent botEvent, int turnNumber) {
        var isNotOld = botEvent.getTurnNumber() >= turnNumber - MAX_EVENT_AGE;
        return isNotOld || botEvent.isCritical();
    }

    private static boolean isOldAndNonCriticalEvent(BotEvent botEvent, int turnNumber) {
        var isOld = botEvent.getTurnNumber() < turnNumber - MAX_EVENT_AGE;
        return isOld && !botEvent.isCritical();
    }

    private void addEvent(BotEvent botEvent) {
        synchronized (events) {
            if (events.size() <= MAX_QUEUE_SIZE) {
                events.add(botEvent);
            } else {
                System.err.println("Maximum event queue size has been reached: " + MAX_QUEUE_SIZE);
            }
        }
    }

    private void addCustomEvents() {
        baseBotInternals.getConditions().stream().filter(Condition::test).forEach(condition ->
                addEvent(new CustomEvent(baseBotInternals.getCurrentTickOrThrow().getTurnNumber(), condition))
        );
    }

    private void dumpEvents() {
        StringJoiner stringJoiner = new StringJoiner(", ");
        events.forEach(event -> stringJoiner.add(event.getClass().getSimpleName() + "(" + event.getTurnNumber() + ")"));
        System.out.println("events: " + stringJoiner);
    }
}