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

    private static boolean isOldEvent(BotEvent event, int currentTurn) {
        return event.getTurnNumber() + MAX_EVENT_AGE < currentTurn;
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
            if (bulletEvent.getVictimId() == baseBot.getMyId()) {
                return EventPriority.onHitByBullet;
            } else {
                return EventPriority.onBulletHit;
            }
        } else if (event instanceof BulletHitBulletEvent) {
            return EventPriority.onBulletHitBullet;
        } else if (event instanceof DeathEvent) {
            DeathEvent deathEvent = (DeathEvent) event;
            if (deathEvent.getVictimId() == baseBot.getMyId()) {
                return EventPriority.onDeath;
            } else {
                return EventPriority.onBotDeath;
            }
        } else if (event instanceof SkippedTurnEvent) {
            return EventPriority.onSkippedTurn;
        } else if (event instanceof CustomEvent) {
            return EventPriority.onCondition;
        } else if (event instanceof WonRoundEvent) {
            return EventPriority.onWonRound;
        } else {
            throw new IllegalStateException("Unhandled event type: " + event);
        }
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

    void addEventsFromTick(TickEvent event, IBaseBot baseBot) {
        if (isDisabled) return;

        addEvent(event, baseBot);
        event.getEvents().forEach(evt -> addEvent(evt, baseBot));

        addCustomEvents(baseBot);
    }

    void setInterruptible(boolean interruptible) {
        Class<? extends BotEvent> eventClass = currentEvent.getClass();
        setInterruptible(eventClass, interruptible);
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

    private void addEvent(BotEvent event, IBaseBot baseBot) {
        if (countEvents() > MAX_QUEUE_SIZE) {
            System.err.println("Maximum event queue size has been reached: " + MAX_QUEUE_SIZE);
        } else {
            int priority = getPriority(event, baseBot);
            var botEvents = eventMap.get(priority);
            if (botEvents == null) {
                botEvents = new CopyOnWriteArrayList<>();
                eventMap.put(priority, botEvents);
            }
            botEvents.add(event);
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
}
