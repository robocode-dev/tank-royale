package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Class similar to BotEventHandlers, but used for internal API event handling only.
 */
final class APIEventHandlers {

    final EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<>();

    final EventHandler<GameEndedEvent> onGameEnded = new EventHandler<>();
    final EventHandler<GameAbortedEvent> onGameAborted = new EventHandler<>();
    final EventHandler<RoundStartedEvent> onRoundStarted = new EventHandler<>();
    final EventHandler<RoundEndedEvent> onRoundEnded = new EventHandler<>();

    final EventHandler<DeathEvent> onDeath = new EventHandler<>();
    final EventHandler<HitBotEvent> onHitBot = new EventHandler<>();
    final EventHandler<HitWallEvent> onHitWall = new EventHandler<>();
    final EventHandler<BulletFiredEvent> onBulletFired = new EventHandler<>();

    // Virtual (fake) event handler
    final EventHandler<TickEvent> onNextTurn = new EventHandler<>();

    private final Map<Class<? extends IEvent>, EventHandler<? extends IEvent>> eventHandlerMap = new HashMap<>();

    APIEventHandlers() {
        initializeEventHandlers();
    }

    private void initializeEventHandlers() {
        eventHandlerMap.put(DisconnectedEvent.class, onDisconnected);

        eventHandlerMap.put(GameEndedEvent.class, onGameEnded);
        eventHandlerMap.put(GameAbortedEvent.class, onGameAborted);
        eventHandlerMap.put(RoundStartedEvent.class, onRoundStarted);
        eventHandlerMap.put(RoundEndedEvent.class, onRoundEnded);

        eventHandlerMap.put(DeathEvent.class, onDeath);
        eventHandlerMap.put(HitBotEvent.class, onHitBot);
        eventHandlerMap.put(HitWallEvent.class, onHitWall);
        eventHandlerMap.put(BulletFiredEvent.class, onBulletFired);

        eventHandlerMap.put(NextTurnEvent.class, onNextTurn);
    }

    @SuppressWarnings("unchecked")
    void fireEvent(BotEvent event) {
        var handler = (EventHandler<IEvent>) eventHandlerMap.get(event.getClass());
        if (handler != null) {
            handler.publish(event);
        }
        // ignore, if there is no registered event handler
    }

    // Virtual (fake) events:

    public static final class GameAbortedEvent implements IEvent {
    }

    private static final class NextTurnEvent implements IEvent {
    }
}