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
            var eventPriority = getPriority(event);

            if (eventPriority < currentTopEventPriority) {
                return; // Exit when event priority is lower than the current event being processed
            }

            // Same event?
            if (eventPriority == currentTopEventPriority) {
                if (!isInterruptible()) {
                    // Ignore same event occurring again, when not interruptible
                    return;
                }
                setInterruptible(event.getClass(), false);
                // The current event handler must be interrupted (by throwing an InterruptEventHandlerException)
                throw new InterruptEventHandlerException();
            }

            int oldTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = eventPriority;
            currentTopEvent = event;

            events.remove(event);

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
            // Lower (older) turn number gives negative delta -> becomes first
            int timeDiff = e1.getTurnNumber() - e2.getTurnNumber();
            if (timeDiff != 0) {
                return timeDiff;
            }
            // Higher priority gives negative delta -> becomes first
            return getPriority(e2) - getPriority(e1);
        });
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

    private int getPriority(BotEvent event) {
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
            return (bulletEvent.getVictimId() == baseBotInternals.getMyId()) ?
                EventPriority.onHitByBullet :
                EventPriority.onBulletHit;
        } else if (event instanceof BulletHitBulletEvent) {
            return EventPriority.onBulletHitBullet;
        } else if (event instanceof DeathEvent) {
            DeathEvent deathEvent = (DeathEvent) event;
            return (deathEvent.getVictimId() == baseBotInternals.getMyId()) ?
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
