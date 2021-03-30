package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.*;

final class BotEventHandlers {

  final IBaseBot baseBot;

  // Regular bot event handlers
  final EventHandler<ConnectedEvent> onConnected = new EventHandler<>();
  final EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<>();
  final EventHandler<ConnectionErrorEvent> onConnectionError = new EventHandler<>();
  final EventHandler<GameStartedEvent> onGameStarted = new EventHandler<>();
  final EventHandler<GameEndedEvent> onGameEnded = new EventHandler<>();
  final EventHandler<RoundStartedEvent> onRoundStarted = new EventHandler<>();
  final EventHandler<RoundEndedEvent> onRoundEnded = new EventHandler<>();
  final EventHandler<TickEvent> onTick = new EventHandler<>();
  final EventHandler<SkippedTurnEvent> onSkippedTurn = new EventHandler<>();
  final EventHandler<DeathEvent> onDeath = new EventHandler<>();
  final EventHandler<DeathEvent> onBotDeath = new EventHandler<>();
  final EventHandler<HitBotEvent> onHitBot = new EventHandler<>();
  final EventHandler<HitWallEvent> onHitWall = new EventHandler<>();
  final EventHandler<BulletFiredEvent> onBulletFired = new EventHandler<>();
  final EventHandler<BulletHitBotEvent> onHitByBullet = new EventHandler<>();
  final EventHandler<BulletHitBotEvent> onBulletHit = new EventHandler<>();
  final EventHandler<BulletHitBulletEvent> onBulletHitBullet = new EventHandler<>();
  final EventHandler<BulletHitWallEvent> onBulletHitWall = new EventHandler<>();
  final EventHandler<ScannedBotEvent> onScannedBot = new EventHandler<>();
  final EventHandler<WonRoundEvent> onWonRound = new EventHandler<>();
  final EventHandler<CustomEvent> onCustomEvent = new EventHandler<>();

  final EventHandler<TickEvent> onNextTurn = new EventHandler<>();

  BotEventHandlers(IBaseBot baseBot) {
    this.baseBot = baseBot;

    onConnected.subscribe(baseBot::onConnected);
    onDisconnected.subscribe(baseBot::onDisconnected);
    onConnectionError.subscribe(baseBot::onConnectionError);
    onGameStarted.subscribe(baseBot::onGameStarted);
    onGameEnded.subscribe(baseBot::onGameEnded);
//    onRoundStarted.subscribe(baseBot::onRoundStarted); // TODO
//    onRoundEnded.subscribe(baseBot::onRoundEnded); // TODO
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
    } else if (event instanceof BulletHitBotEvent) {
      BulletHitBotEvent bulletEvent = (BulletHitBotEvent) event;
      if (bulletEvent.getVictimId() == baseBot.getMyId()) {
        onHitByBullet.publish((BulletHitBotEvent) event);
      } else {
        onBulletHit.publish((BulletHitBotEvent) event);
      }
    } else if (event instanceof DeathEvent) {
      DeathEvent deathEvent = (DeathEvent) event;
      if (deathEvent.getVictimId() == baseBot.getMyId()) {
        onDeath.publish((DeathEvent) event);
      } else {
        onBotDeath.publish((DeathEvent) event);
      }
    } else if (event instanceof BulletHitBulletEvent) {
      onBulletHitBullet.publish((BulletHitBulletEvent) event);
    } else if (event instanceof RoundStartedEvent) {
      onRoundStarted.publish((RoundStartedEvent) event);
    } else if (event instanceof RoundEndedEvent) {
      onRoundEnded.publish((RoundEndedEvent) event);
    } else if (event instanceof WonRoundEvent) {
      onWonRound.publish((WonRoundEvent) event);
    } else if (event instanceof CustomEvent) {
      onCustomEvent.publish((CustomEvent) event);
    } else {
      throw new IllegalStateException("Unhandled event type: " + event);
    }
  }
}