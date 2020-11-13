package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

class EventQueue {

  private final int MAX_EVENT_AGE = 2; // turns

  private final BotEvents botEvents;

  private final Map<Integer, BotEvent> eventMap = new ConcurrentHashMap<>();

  public EventQueue(BotEvents botEvents) {
    this.botEvents = botEvents;
  }

  public void clear() {
    eventMap.clear();
  }

  private void addEvent(IBaseBot baseBot, BotEvent event) {
    int priority;

    if (event instanceof TickEvent) {
      priority = EventPriority.onTick;
    } else if (event instanceof ScannedBotEvent) {
      priority = EventPriority.onScannedBot;
    } else if (event instanceof SkippedTurnEvent) {
      priority = EventPriority.onSkippedTurn;
    } else if (event instanceof HitBotEvent) {
      priority = EventPriority.onHitBot;
    } else if (event instanceof HitWallEvent) {
      priority = EventPriority.onHitWall;
    } else if (event instanceof BulletFiredEvent) {
      priority = EventPriority.onBulletFired;
    } else if (event instanceof BulletHitWallEvent) {
      priority = EventPriority.onBulletHitWall;
    } else if (event instanceof BulletHitBotEvent) {
      BulletHitBotEvent bulletEvent = (BulletHitBotEvent) event;
      if (bulletEvent.getVictimId() == baseBot.getMyId()) {
        priority = EventPriority.onHitByBullet;
      } else {
        priority = EventPriority.onBulletHit;
      }
    } else if (event instanceof DeathEvent) {
      DeathEvent deathEvent = (DeathEvent) event;
      if (deathEvent.getVictimId() == baseBot.getMyId()) {
        priority = EventPriority.onDeath;
      } else {
        priority = EventPriority.onBotDeath;
      }
    } else if (event instanceof BulletHitBulletEvent) {
      priority = EventPriority.onBulletHitBullet;
    } else if (event instanceof WonRoundEvent) {
      priority = EventPriority.onWonRound;
    } else if (event instanceof CustomEvent) {
      priority = EventPriority.onCondition;
    } else {
      throw new IllegalStateException("Unhandled event type: " + event);
    }
    eventMap.put(priority, event);
  }

  public void addEventsFromTick(IBaseBot baseBot, TickEvent event) {
    addEvent(baseBot, event);
    event.getEvents().forEach(e -> addEvent(baseBot, e));
  }

  public void dispatchEvents(IBaseBot baseBot, int currentTurnNumber) {
    // Remove all old entries
    eventMap.values().removeIf(event -> currentTurnNumber > event.getTurnNumber() + MAX_EVENT_AGE);

    // Publish all event in the order of the keys, i.e. event priority order
    Iterator<BotEvent> iterator = eventMap.values().iterator();
    while (iterator.hasNext()) {
      BotEvent event = iterator.next();
      iterator.remove();
      dispatch(baseBot, event);
    }
  }

  private void dispatch(IBaseBot baseBot, BotEvent event) {
    if (event instanceof TickEvent) {
      botEvents.onTick.publish((TickEvent) event);
    } else if (event instanceof ScannedBotEvent) {
      botEvents.onScannedBot.publish((ScannedBotEvent) event);
    } else if (event instanceof SkippedTurnEvent) {
      botEvents.onSkippedTurn.publish((SkippedTurnEvent) event);
    } else if (event instanceof HitBotEvent) {
      botEvents.onHitBot.publish((HitBotEvent) event);
    } else if (event instanceof HitWallEvent) {
      botEvents.onHitWall.publish((HitWallEvent) event);
    } else if (event instanceof BulletFiredEvent) {
      botEvents.onBulletFired.publish((BulletFiredEvent) event);
    } else if (event instanceof BulletHitWallEvent) {
      botEvents.onBulletHitWall.publish((BulletHitWallEvent) event);
    } else if (event instanceof BulletHitBotEvent) {
      BulletHitBotEvent bulletEvent = (BulletHitBotEvent) event;
      if (bulletEvent.getVictimId() == baseBot.getMyId()) {
        botEvents.onHitByBullet.publish((BulletHitBotEvent) event);
      } else {
        botEvents.onBulletHit.publish((BulletHitBotEvent) event);
      }
    } else if (event instanceof DeathEvent) {
      DeathEvent deathEvent = (DeathEvent) event;
      if (deathEvent.getVictimId() == baseBot.getMyId()) {
        botEvents.onDeath.publish((DeathEvent) event);
      } else {
        botEvents.onBotDeath.publish((DeathEvent) event);
      }
    } else if (event instanceof BulletHitBulletEvent) {
      botEvents.onBulletHitBullet.publish((BulletHitBulletEvent) event);
    } else if (event instanceof WonRoundEvent) {
      botEvents.onWonRound.publish((WonRoundEvent) event);
    } else if (event instanceof CustomEvent) {
      botEvents.onCustomEvent.publish((CustomEvent) event);
    } else {
      throw new IllegalStateException("Unhandled event type: " + event);
    }
  }
}
