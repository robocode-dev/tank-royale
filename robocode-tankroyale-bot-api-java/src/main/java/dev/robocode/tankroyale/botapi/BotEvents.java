package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

class BotEvents {

  protected final IBaseBot baseBot;

  // Events
  final Event<ConnectedEvent> onConnected = new Event<>();
  final Event<DisconnectedEvent> onDisconnected = new Event<>();
  final Event<ConnectionErrorEvent> onConnectionError = new Event<>();
  final Event<GameStartedEvent> onGameStarted = new Event<>();
  final Event<GameEndedEvent> onGameEnded = new Event<>();
  final Event<TickEvent> onTick = new Event<>();
  final Event<SkippedTurnEvent> onSkippedTurn = new Event<>();
  final Event<BotDeathEvent> onDeath = new Event<>();
  final Event<BotDeathEvent> onBotDeath = new Event<>();
  final Event<BotHitBotEvent> onHitBot = new Event<>();
  final Event<BotHitWallEvent> onHitWall = new Event<>();
  final Event<BulletFiredEvent> onBulletFired = new Event<>();
  final Event<BulletHitBotEvent> onHitByBullet = new Event<>();
  final Event<BulletHitBotEvent> onBulletHit = new Event<>();
  final Event<BulletHitBulletEvent> onBulletHitBullet = new Event<>();
  final Event<BulletHitWallEvent> onBulletHitWall = new Event<>();
  final Event<ScannedBotEvent> onScannedBot = new Event<>();
  final Event<WonRoundEvent> onWonRound = new Event<>();

  BotEvents(IBaseBot baseBot) {
    this.baseBot = baseBot;
  }

  protected void dispatchEvents(TickEvent tickEvent) {
    tickEvent
        .getEvents()
        .forEach(
            event -> {
              if (event instanceof BotDeathEvent) {
                BotDeathEvent botDeathEvent = (BotDeathEvent) event;
                if (botDeathEvent.getVictimId() == baseBot.getMyId()) {
                  onDeath.publish((BotDeathEvent) event);
                } else {
                  onBotDeath.publish((BotDeathEvent) event);
                }

              } else if (event instanceof BotHitBotEvent) {
                onHitBot.publish((BotHitBotEvent) event);

              } else if (event instanceof BotHitWallEvent) {
                onHitWall.publish((BotHitWallEvent) event);

              } else if (event instanceof BulletFiredEvent) {
                onBulletFired.publish((BulletFiredEvent) event);

              } else if (event instanceof BulletHitBotEvent) {
                BulletHitBotEvent bulletEvent = (BulletHitBotEvent) event;
                if (bulletEvent.getVictimId() == baseBot.getMyId()) {
                  onHitByBullet.publish(bulletEvent);
                } else {
                  onBulletHit.publish(bulletEvent);
                }

              } else if (event instanceof BulletHitBulletEvent) {
                onBulletHitBullet.publish((BulletHitBulletEvent) event);

              } else if (event instanceof BulletHitWallEvent) {
                onBulletHitWall.publish((BulletHitWallEvent) event);

              } else if (event instanceof ScannedBotEvent) {
                onScannedBot.publish((ScannedBotEvent) event);

              } else if (event instanceof SkippedTurnEvent) {
                onSkippedTurn.publish((SkippedTurnEvent) event);

              } else if (event instanceof WonRoundEvent) {
                onWonRound.publish((WonRoundEvent) event);

              } else {
                System.err.println("Unhandled event: " + event);
              }
            });
  }

  // Event handler which events in the order they have been added to the handler
  protected static class Event<T> {
    private final List<Consumer<T>> subscribers = Collections.synchronizedList(new ArrayList<>());

    final void subscribe(Consumer<T> subscriber) {
      subscribers.add(subscriber);
    }

    final void publish(T event) {
      for (Consumer<T> subscriber : subscribers) {
        subscriber.accept(event);
      }
    }
  }
}
