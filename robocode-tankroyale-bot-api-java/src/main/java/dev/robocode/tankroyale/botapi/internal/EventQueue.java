package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

final class EventQueue {

  private final int MAX_EVENT_AGE = 2; // turns

  private final BaseBotInternals baseBotInternals;
  private final BotEventHandlers botEventHandlers;

  private final SortedMap<Integer, List<BotEvent>> eventMap = new ConcurrentSkipListMap<>();

  public EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers) {
    this.baseBotInternals = baseBotInternals;
    this.botEventHandlers = botEventHandlers;
  }

  void clear() {
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

    List<BotEvent> botEvents = eventMap.get(priority);
    if (botEvents == null) {
      botEvents = new CopyOnWriteArrayList<>();
      eventMap.put(priority, botEvents);
    }
    botEvents.add(event);
  }

  void addEventsFromTick(IBaseBot baseBot, TickEvent event) {
    addEvent(baseBot, event);
    event.getEvents().forEach(e -> addEvent(baseBot, e));

    addCustomEvents(baseBot);
  }

  private void addCustomEvents(IBaseBot baseBot) {
    baseBotInternals
        .getConditions()
        .forEach(
            condition -> {
              if (condition.test()) {
                addEvent(
                    baseBot,
                    new CustomEvent(baseBotInternals.getCurrentTick().getTurnNumber(), condition));
              }
            });
  }

  void dispatchEvents(int currentTurnNumber) {
    // Remove all old entries
    eventMap
        .values()
        .forEach(
            list ->
                list.removeIf(event -> currentTurnNumber > event.getTurnNumber() + MAX_EVENT_AGE));

    // Publish all event in the order of the keys, i.e. event priority order
    Iterator<Map.Entry<Integer, List<BotEvent>>> iterator = eventMap.entrySet().iterator();
    while (iterator.hasNext()) {
      List<BotEvent> events = iterator.next().getValue();
      iterator.remove();

      events.forEach(botEventHandlers::fire);
    }
  }
}
