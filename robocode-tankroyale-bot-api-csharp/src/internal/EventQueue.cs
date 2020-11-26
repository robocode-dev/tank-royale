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

    internal void AddEventsFromTick(TickEvent tickEvent, IBaseBot baseBot)
    {
      AddEvent(tickEvent, baseBot);

      IEnumerator<BotEvent> enumerator = tickEvent.Events.GetEnumerator();
      while (enumerator.MoveNext())
      {
        var botEvent = enumerator.Current;
        AddEvent(botEvent, baseBot);
      }
      AddCustomEvents(baseBot);
    }

    internal void DispatchEvents(int currentTurnNumber)
    {
      RemoveOldEvents(currentTurnNumber);

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

    private void AddEvent(BotEvent botEvent, IBaseBot baseBot)
    {
      int priority = GetPriority(botEvent, baseBot);

      ConcurrentQueue<BotEvent> events;
      eventsDict.TryGetValue(priority, out events);
      if (events == null)
      {
        events = new ConcurrentQueue<BotEvent>();
        eventsDict.Add(priority, events);
      }
      events.Enqueue(botEvent);
    }

    private void AddCustomEvents(IBaseBot baseBot)
    {
      foreach (Events.Condition condition in baseBotInternals.Conditions)
      {
        if (condition.Test())
        {
          AddEvent(new CustomEvent(baseBotInternals.CurrentTick.TurnNumber, condition), baseBot);
        }
      }
    }

    private void RemoveOldEvents(int currentTurnNumber)
    {
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
    }

    private static int GetPriority(BotEvent botEvent, IBaseBot baseBot)
    {
      if (botEvent is TickEvent)
      {
        return EventPriority.OnTick;
      }
      else if (botEvent is ScannedBotEvent)
      {
        return EventPriority.OnScannedBot;
      }
      else if (botEvent is HitBotEvent)
      {
        return EventPriority.OnHitBot;
      }
      else if (botEvent is HitWallEvent)
      {
        return EventPriority.OnHitWall;
      }
      else if (botEvent is BulletFiredEvent)
      {
        return EventPriority.OnBulletFired;
      }
      else if (botEvent is BulletHitWallEvent)
      {
        return EventPriority.OnBulletHitWall;
      }
      else if (botEvent is BulletHitBotEvent)
      {
        BulletHitBotEvent bulletEvent = (BulletHitBotEvent)botEvent;
        if (bulletEvent.VictimId == baseBot.MyId)
        {
          return EventPriority.OnHitByBullet;
        }
        else
        {
          return EventPriority.OnBulletHit;
        }
      }
      else if (botEvent is BulletHitBulletEvent)
      {
        return EventPriority.OnBulletHitBullet;
      }
      else if (botEvent is DeathEvent)
      {
        DeathEvent deathEvent = (DeathEvent)botEvent;
        if (deathEvent.VictimId == baseBot.MyId)
        {
          return EventPriority.OnDeath;
        }
        else
        {
          return EventPriority.OnBotDeath;
        }
      }
      else if (botEvent is SkippedTurnEvent)
      {
        return EventPriority.OnSkippedTurn;
      }
      else if (botEvent is CustomEvent)
      {
        return EventPriority.OnCondition;
      }
      else if (botEvent is WonRoundEvent)
      {
        return EventPriority.OnWonRound;
      }
      else
      {
        throw new InvalidOperationException("Unhandled event type: " + botEvent);
      }
    }
  }
}