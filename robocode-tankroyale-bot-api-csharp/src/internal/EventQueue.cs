using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal sealed class EventQueue
  {
    private const int MaxEventAge = 2; // turns

    private readonly BaseBotInternals baseBotInternals;
    private readonly BotEventHandlers botEventHandlers;

    private readonly IDictionary<int, ConcurrentQueue<BotEvent>> eventsDict = new ConcurrentDictionary<int, ConcurrentQueue<BotEvent>>();

    internal EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers)
    {
      this.baseBotInternals = baseBotInternals;
      this.botEventHandlers = botEventHandlers;
    }

    public void Clear()
    {
      eventsDict.Clear();
    }

    private void AddEvent(IBaseBot baseBot, BotEvent botEvent)
    {
      int priority;

      if (botEvent is TickEvent)
      {
        priority = EventPriority.OnTick;
      }
      else if (botEvent is ScannedBotEvent)
      {
        priority = EventPriority.OnScannedBot;
      }
      else if (botEvent is SkippedTurnEvent)
      {
        priority = EventPriority.OnSkippedTurn;
      }
      else if (botEvent is HitBotEvent)
      {
        priority = EventPriority.OnHitBot;
      }
      else if (botEvent is HitWallEvent)
      {
        priority = EventPriority.OnHitWall;
      }
      else if (botEvent is BulletFiredEvent)
      {
        priority = EventPriority.OnBulletFired;
      }
      else if (botEvent is BulletHitWallEvent)
      {
        priority = EventPriority.OnBulletHitWall;
      }
      else if (botEvent is BulletHitBotEvent)
      {
        BulletHitBotEvent bulletEvent = (BulletHitBotEvent)botEvent;
        if (bulletEvent.VictimId == baseBot.MyId)
        {
          priority = EventPriority.OnHitByBullet;
        }
        else
        {
          priority = EventPriority.OnBulletHit;
        }
      }
      else if (botEvent is DeathEvent)
      {
        DeathEvent deathEvent = (DeathEvent)botEvent;
        if (deathEvent.VictimId == baseBot.MyId)
        {
          priority = EventPriority.OnDeath;
        }
        else
        {
          priority = EventPriority.OnBotDeath;
        }
      }
      else if (botEvent is BulletHitBulletEvent)
      {
        priority = EventPriority.OnBulletHitBullet;
      }
      else if (botEvent is WonRoundEvent)
      {
        priority = EventPriority.OnWonRound;
      }
      else if (botEvent is CustomEvent)
      {
        priority = EventPriority.OnCondition;
      }
      else
      {
        throw new InvalidOperationException("Unhandled event type: " + botEvent);
      }

      ConcurrentQueue<BotEvent> events;
      eventsDict.TryGetValue(priority, out events);
      if (events == null)
      {
        events = new ConcurrentQueue<BotEvent>();
        eventsDict.Add(priority, events);
      }
      events.Enqueue(botEvent);
    }

    internal void AddEventsFromTick(IBaseBot baseBot, TickEvent tickEvent)
    {
      AddEvent(baseBot, tickEvent);

      IEnumerator<BotEvent> enumerator = tickEvent.Events.GetEnumerator();
      while (enumerator.MoveNext())
      {
        var botEvent = enumerator.Current;
        AddEvent(baseBot, botEvent);
      }
      AddCustomEvents(baseBot);
    }

    private void AddCustomEvents(IBaseBot baseBot)
    {
      foreach (Events.Condition condition in baseBotInternals.Conditions)
      {
        if (condition.Test())
        {
          AddEvent(baseBot, new CustomEvent(baseBotInternals.CurrentTick.TurnNumber, condition));
        }
      }
    }

    internal void DispatchEvents(int currentTurnNumber)
    {
      // Remove all old entries
      foreach (var item in eventsDict)
      {
        var events = item.Value;
        foreach (var botEvent in events)
        {
          if (botEvent.TurnNumber < currentTurnNumber - MaxEventAge)
          {
            eventsDict.Remove(item.Key);
          }
        }
      }

      // Publish all event in the order of the keys, i.e. event priority order
      var sortedDict = new SortedDictionary<int, ConcurrentQueue<BotEvent>>(eventsDict);

      foreach (var item in sortedDict)
      {
        var events = item.Value;
        for (int i = 0; i < events.Count; i++)
        {
          BotEvent botEvent;
          if (events.TryDequeue(out botEvent))
          {
            botEventHandlers.Fire(botEvent);
          }
        }
      }
    }
  }
}