package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

final class EventQueue {

    private static final int MAX_QUEUE_SIZE = 256;
    private static final int MAX_EVENT_AGE = 2;

    private final BaseBotInternals baseBotInternals;
    private final BotEventHandlers botEventHandlers;

    private final SortedMap<Integer, List<BotEvent>> eventMap = new ConcurrentSkipListMap<>();

    private BotEvent currentEvent;

    private final Set<Class<? extends BotEvent>> interruptibles = new HashSet<>();

    private boolean isDisabled;

    public EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers) {
        this.baseBotInternals = baseBotInternals;
        this.botEventHandlers = botEventHandlers;
    }

    void clear() {
        eventMap.clear();
        baseBotInternals.getConditions().clear(); // conditions might be added in the bots run() method each round
        currentEvent = null;
        isDisabled = false;
    }

    void disable() {
        isDisabled = true;
    }

    void setInterruptible(boolean interruptible) {
        setInterruptible(currentEvent.getClass(), interruptible);
    }

    void setInterruptible(Class<? extends BotEvent> eventClass, boolean interruptible) {
        if (interruptible) {
            interruptibles.add(eventClass);
        } else {
            interruptibles.remove(eventClass);
        }
    }

    private boolean isInterruptible() {
        return interruptibles.contains(currentEvent.getClass());
    }

    void addEventsFromTick(TickEvent event, IBaseBot baseBot) {
        if (isDisabled) return;

        addEvent(event, baseBot);
        event.getEvents().forEach(evt -> addEvent(evt, baseBot));

        addCustomEvents(baseBot);
    }

    void dispatchEvents(int currentTurn) {
        removeOldEvents(currentTurn);

        eventMap.values().forEach(eventList -> eventList.forEach(event -> {

            // Inside same event handler?
            if (currentEvent != null && event.getClass().equals(currentEvent.getClass())) {
                if (isInterruptible()) {
                    setInterruptible(event.getClass(), false);
                    throw new InterruptEventHandlerException();
                }
                return; // ignore same event occurring again, when not interruptible
            }

            // Dispatch event
            try {
                currentEvent = event;
                eventList.remove(event); // remove event prior to handling it
                botEventHandlers.fire(event);

                setInterruptible(event.getClass(), false);

            } catch (InterruptEventHandlerException ignore) {
                // Expected
            } finally {
                currentEvent = null;
            }
        }));
    }

    private void removeOldEvents(int currentTurn) {
        // Only remove old events that are not critical
        eventMap.values().forEach(eventList ->
                eventList.removeIf(event -> !event.isCritical() && isOldEvent(event, currentTurn)));
    }

    private static boolean isOldEvent(BotEvent event, int currentTurn) {
        return event.getTurnNumber() + MAX_EVENT_AGE < currentTurn;
    }

    private void addEvent(BotEvent event, IBaseBot baseBot) {
        if (countEvents() > MAX_QUEUE_SIZE) {
            System.err.println("Maximum event queue size has been reached: " + MAX_QUEUE_SIZE);
        } else {
            int priority = getPriority(event, baseBot);
            var events = eventMap.get(priority);
            if (events == null) {
                events = new CopyOnWriteArrayList<>();
                eventMap.put(priority, events);
            }
            events.add(event);
        }
    }

    private int countEvents() {
        return eventMap.values().stream().mapToInt(List::size).sum();
    }

    private void addCustomEvents(IBaseBot baseBot) {
        baseBotInternals.getConditions().stream().filter(Condition::test).forEach(condition ->
                addEvent(new CustomEvent(baseBotInternals.getCurrentTick().getTurnNumber(), condition), baseBot)
        );
    }

    private static int getPriority(BotEvent event, IBaseBot baseBot) {
        if (event instanceof TickEvent) {
            return EventPriority.onTick;
        } else if (event instanceof ScannedBotEvent) {
            return EventPriority.onScannedBot;
        } else if (event instanceof HitBotEvent) {
            return EventPriority.onHitBot;
        } else if (event instanceof HitWallEvent) {
            return EventPriority.onHitWall;
        } else if (event instanceof BulletFiredEvent) {
            return EventPriority.onBulletFired;
        } else if (event instanceof BulletHitWallEvent) {
            return EventPriority.onBulletHitWall;
        } else if (event instanceof BulletHitBotEvent) {
            BulletHitBotEvent bulletEvent = (BulletHitBotEvent) event;
            return (bulletEvent.getVictimId() == baseBot.getMyId()) ?
                EventPriority.onHitByBullet :
                EventPriority.onBulletHit;
        } else if (event instanceof BulletHitBulletEvent) {
            return EventPriority.onBulletHitBullet;
        } else if (event instanceof DeathEvent) {
            DeathEvent deathEvent = (DeathEvent) event;
            return (deathEvent.getVictimId() == baseBot.getMyId()) ?
                EventPriority.onDeath :
                EventPriority.onBotDeath;
        } else if (event instanceof SkippedTurnEvent) {
            return EventPriority.onSkippedTurn;
        } else if (event instanceof CustomEvent) {
            return EventPriority.onCondition;
        } else if (event instanceof WonRoundEvent) {
            return EventPriority.onWonRound;
        }
        throw new IllegalStateException("Unhandled event type: " + event);
    }
}
