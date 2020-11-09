package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

final class BotEvents {

  final EventHandler<TickEvent> onProcessTurn = new EventHandler<>();
  final EventHandler<TickEvent> onNewRound = new EventHandler<>();

  // Event handlers
  final EventHandler<ConnectedEvent> onConnected = new EventHandler<>();
  final EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<>();
  final EventHandler<ConnectionErrorEvent> onConnectionError = new EventHandler<>();
  final EventHandler<GameStartedEvent> onGameStarted = new EventHandler<>();
  final EventHandler<GameEndedEvent> onGameEnded = new EventHandler<>();
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

  BotEvents(IBaseBot baseBot) {
    onConnected.subscribe(baseBot::onConnected);
    onDisconnected.subscribe(baseBot::onDisconnected);
    onConnectionError.subscribe(baseBot::onConnectionError);
    onGameStarted.subscribe(baseBot::onGameStarted);
    onGameEnded.subscribe(baseBot::onGameEnded);
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
}