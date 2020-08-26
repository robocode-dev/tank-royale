package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
  final Event<Condition> onCondition = new Event<>();

  BotEvents(IBaseBot baseBot) {
    this.baseBot = baseBot;
    init();
  }

  private void init() {
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
    onCondition.subscribe(baseBot::onCondition);
  }

  protected void fireEvents(TickEvent tickEvent) {
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

  protected void fireConditionMet(Condition condition) {
    onCondition.publish(condition);
  }

  // Event handler which events in the order they have been added to the handler
  protected static class Event<T> {
    private final List<EntryWithPriority> subscriberEntries = Collections.synchronizedList(new ArrayList<>());

    final void subscribe(Consumer<T> subscriber, int priority) {
      subscriberEntries.add(new EntryWithPriority(subscriber, priority));
    }

    final void subscribe(Consumer<T> subscriber) {
      subscribe(subscriber, 1);
    }

    final void publish(T event) {
      subscriberEntries.sort(new EntryWithPriorityComparator());
      for (EntryWithPriority entry : subscriberEntries) {
        entry.subscriber.accept(event);
      }
    }

    class EntryWithPriority {
      private final int priority; // Lower values means lower priority
      private final Consumer<T> subscriber;

      EntryWithPriority(Consumer<T> subscriber, int priority) {
        this.subscriber = subscriber;
        this.priority = priority;
      }
    }

    class EntryWithPriorityComparator implements Comparator<EntryWithPriority> {
      @Override
      public int compare(EntryWithPriority e1, EntryWithPriority e2) {
        return e2.priority - e1.priority;
      }
    }
  }
}