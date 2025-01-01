package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used for bot event handlers on the public API that are may or may not be triggered bot event queue, and might
 * not be handled immediately by the bot logic.
 */
final class BotEventHandlers {

    final EventHandler<ConnectedEvent> onConnected = new EventHandler<>();
    final EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<>();
    final EventHandler<ConnectionErrorEvent> onConnectionError = new EventHandler<>();

    final EventHandler<GameStartedEvent> onGameStarted = new EventHandler<>();
    final EventHandler<GameEndedEvent> onGameEnded = new EventHandler<>();
    final EventHandler<GameAbortedEvent> onGameAborted = new EventHandler<>();
    final EventHandler<RoundStartedEvent> onRoundStarted = new EventHandler<>();
    final EventHandler<RoundEndedEvent> onRoundEnded = new EventHandler<>();

    final EventHandler<TickEvent> onTick = new EventHandler<>();
    final EventHandler<SkippedTurnEvent> onSkippedTurn = new EventHandler<>();
    final EventHandler<DeathEvent> onDeath = new EventHandler<>();
    final EventHandler<BotDeathEvent> onBotDeath = new EventHandler<>();
    final EventHandler<HitBotEvent> onHitBot = new EventHandler<>();
    final EventHandler<HitWallEvent> onHitWall = new EventHandler<>();
    final EventHandler<BulletFiredEvent> onBulletFired = new EventHandler<>();
    final EventHandler<HitByBulletEvent> onHitByBullet = new EventHandler<>();
    final EventHandler<BulletHitBotEvent> onBulletHit = new EventHandler<>();
    final EventHandler<BulletHitBulletEvent> onBulletHitBullet = new EventHandler<>();
    final EventHandler<BulletHitWallEvent> onBulletHitWall = new EventHandler<>();
    final EventHandler<ScannedBotEvent> onScannedBot = new EventHandler<>();
    final EventHandler<WonRoundEvent> onWonRound = new EventHandler<>();
    final EventHandler<CustomEvent> onCustomEvent = new EventHandler<>();
    final EventHandler<TeamMessageEvent> onTeamMessage = new EventHandler<>();

    private final Map<Class<? extends IEvent>, EventHandler<? extends IEvent>> eventHandlerMap = new HashMap<>();


    BotEventHandlers(IBaseBot baseBot) {
        initializeEventHandlers();
        subscribeToEventHandlers(baseBot);
    }

    private void initializeEventHandlers() {
        eventHandlerMap.put(ConnectedEvent.class, onConnected);
        eventHandlerMap.put(DisconnectedEvent.class, onDisconnected);
        eventHandlerMap.put(ConnectionErrorEvent.class, onConnectionError);

        eventHandlerMap.put(GameStartedEvent.class, onGameStarted);
        eventHandlerMap.put(GameEndedEvent.class, onGameEnded);
        eventHandlerMap.put(GameAbortedEvent.class, onGameAborted);
        eventHandlerMap.put(RoundStartedEvent.class, onRoundStarted);
        eventHandlerMap.put(RoundEndedEvent.class, onRoundEnded);

        eventHandlerMap.put(TickEvent.class, onTick);
        eventHandlerMap.put(SkippedTurnEvent.class, onSkippedTurn);
        eventHandlerMap.put(DeathEvent.class, onDeath);
        eventHandlerMap.put(BotDeathEvent.class, onBotDeath);
        eventHandlerMap.put(HitBotEvent.class, onHitBot);
        eventHandlerMap.put(HitWallEvent.class, onHitWall);
        eventHandlerMap.put(BulletFiredEvent.class, onBulletFired);
        eventHandlerMap.put(HitByBulletEvent.class, onHitByBullet);
        eventHandlerMap.put(BulletHitBotEvent.class, onBulletHit);
        eventHandlerMap.put(BulletHitBulletEvent.class, onBulletHitBullet);
        eventHandlerMap.put(BulletHitWallEvent.class, onBulletHitWall);
        eventHandlerMap.put(ScannedBotEvent.class, onScannedBot);
        eventHandlerMap.put(WonRoundEvent.class, onWonRound);
        eventHandlerMap.put(CustomEvent.class, onCustomEvent);
        eventHandlerMap.put(TeamMessageEvent.class, onTeamMessage);
    }

    private void subscribeToEventHandlers(IBaseBot baseBot) {
        onConnected.subscribe(baseBot::onConnected);
        onDisconnected.subscribe(baseBot::onDisconnected);
        onConnectionError.subscribe(baseBot::onConnectionError);

        onGameStarted.subscribe(baseBot::onGameStarted);
        onGameEnded.subscribe(baseBot::onGameEnded);
        onRoundStarted.subscribe(baseBot::onRoundStarted);
        onRoundEnded.subscribe(baseBot::onRoundEnded);

        onTick.subscribe(baseBot::onTick);
        onSkippedTurn.subscribe(baseBot::onSkippedTurn);
        onDeath.subscribe(baseBot::onDeath);
        onBotDeath.subscribe(baseBot::onBotDeath);
        onHitBot.subscribe(baseBot::onHitBot);
        onHitWall.subscribe(baseBot::onHitWall);
        onBulletFired.subscribe(baseBot::onBulletFired);
        onHitByBullet.subscribe(baseBot::onHitByBullet);
        onBulletHit.subscribe(baseBot::onBulletHit);
        onBulletHitBullet.subscribe(baseBot::onBulletHitBullet);
        onBulletHitWall.subscribe(baseBot::onBulletHitWall);
        onScannedBot.subscribe(baseBot::onScannedBot);
        onWonRound.subscribe(baseBot::onWonRound);
        onCustomEvent.subscribe(baseBot::onCustomEvent);
        onTeamMessage.subscribe(baseBot::onTeamMessage);
    }

    @SuppressWarnings("unchecked")
    void fireEvent(BotEvent event) {
        var handler = (EventHandler<IEvent>) eventHandlerMap.get(event.getClass());
        if (handler != null) {
            handler.publish(event);
        } else {
            throw new IllegalStateException("Unhandled event type: " + event);
        }
    }

    // Virtual (fake) event:
    private static final class GameAbortedEvent implements IEvent {
    }
}