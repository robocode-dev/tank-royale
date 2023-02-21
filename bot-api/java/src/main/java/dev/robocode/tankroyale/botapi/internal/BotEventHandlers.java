package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.*;

final class BotEventHandlers {

    final EventHandler<ConnectedEvent> onConnected = new EventHandler<>();
    final EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<>();
    final EventHandler<ConnectionErrorEvent> onConnectionError = new EventHandler<>();

    final EventHandler<GameStartedEvent> onGameStarted = new EventHandler<>();
    final EventHandler<GameEndedEvent> onGameEnded = new EventHandler<>();
    final EventHandler<Void> onGameAborted = new EventHandler<>();
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

    final EventHandler<TickEvent> onNextTurn = new EventHandler<>();

    BotEventHandlers(IBaseBot baseBot) {
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

    void fire(BotEvent event) {
        if (event instanceof TickEvent) {
            onTick.publish((TickEvent) event);
        } else if (event instanceof ScannedBotEvent) {
            onScannedBot.publish((ScannedBotEvent) event);
        } else if (event instanceof SkippedTurnEvent) {
            onSkippedTurn.publish((SkippedTurnEvent) event);
        } else if (event instanceof HitBotEvent) {
            onHitBot.publish((HitBotEvent) event);
        } else if (event instanceof HitWallEvent) {
            onHitWall.publish((HitWallEvent) event);
        } else if (event instanceof BulletFiredEvent) {
            onBulletFired.publish((BulletFiredEvent) event);
        } else if (event instanceof BulletHitWallEvent) {
            onBulletHitWall.publish((BulletHitWallEvent) event);
        } else if (event instanceof HitByBulletEvent) {
            onHitByBullet.publish((HitByBulletEvent) event);
        } else if (event instanceof BulletHitBotEvent) {
            onBulletHit.publish((BulletHitBotEvent) event);
        } else if (event instanceof BotDeathEvent) {
            onBotDeath.publish((BotDeathEvent) event);
        } else if (event instanceof DeathEvent) {
            onDeath.publish((DeathEvent) event);
        } else if (event instanceof BulletHitBulletEvent) {
            onBulletHitBullet.publish((BulletHitBulletEvent) event);
        } else if (event instanceof WonRoundEvent) {
            onWonRound.publish((WonRoundEvent) event);
        } else if (event instanceof CustomEvent) {
            onCustomEvent.publish((CustomEvent) event);
        } else if (event instanceof TeamMessageEvent) {
            onTeamMessage.publish((TeamMessageEvent) event);
        } else {
            throw new IllegalStateException("Unhandled event type: " + event);
        }
    }
}