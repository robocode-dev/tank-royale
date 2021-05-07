using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal sealed class EventQueue
  {
    private const int MaxQueueSize = 256;
    private const int MaxEventAge = 2;

    private readonly BaseBotInternals baseBotInternals;
    private readonly BotEventHandlers botEventHandlers;

    private readonly IDictionary<int, ConcurrentQueue<BotEvent>> eventsDict = new ConcurrentDictionary<int, ConcurrentQueue<BotEvent>>();

    private BotEvent currentEvent;

    internal EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers)
    {
      this.baseBotInternals = baseBotInternals;
      this.botEventHandlers = botEventHandlers;
    }

    public void Clear()
    {
      eventsDict.Clear();
      baseBotInternals.Conditions.Clear(); // conditions might be added in the bot's Run() method each round
      currentEvent = null;
    }

    internal void AddEventsFromTick(TickEvent tickEvent, IBaseBot baseBot)
    {
      AddEvent(tickEvent, baseBot);
      foreach (var botEvent in tickEvent.Events)
      {
        AddEvent(botEvent, baseBot);
      }
      AddCustomEvents(baseBot);
    }

    internal void DispatchEvents(int currentTurn)
    {
      RemoveOldEvents(currentTurn);

      // Handle events in the order of the keys, i.e. event priority order
      var sortedDict = new SortedDictionary<int, ConcurrentQueue<BotEvent>>(eventsDict);

      foreach (var item in sortedDict)
      {
        var events = item.Value;
        foreach (var evt in events)
        {
          // Exit if we are inside an event handler handling the current event being fired
          if (currentEvent != null && events.GetType().Equals(currentEvent.GetType()))
          {
            return;
          }

          try
          {
            currentEvent = evt;
            BotEvent botEvent;
            if (events.TryDequeue(out botEvent))
            {
              botEventHandlers.Fire(botEvent);
            }
          }
          catch (RescanException)
          {
            currentEvent = null;
          }
          catch (Exception)
          {
            currentEvent = null;
            throw;
          }
        }
      }
    }

    private void RemoveOldEvents(int currentTurn)
    {
      foreach (var item in eventsDict)
      {
        var events = item.Value;
        foreach (var botEvent in events)
        {
          if (!botEvent.IsCritical && IsOldEvent(botEvent, currentTurn))
          {
            eventsDict.Remove(item.Key);
          }
        }
      }
    }

    private static bool IsOldEvent(BotEvent botEvent, int currentTurn)
    {
      return botEvent.TurnNumber + MaxEventAge < currentTurn;
    }

    private void AddEvent(BotEvent botEvent, IBaseBot baseBot)
    {
      if (CountEvents() > MaxQueueSize)
      {
        Console.Error.WriteLine($"Maximum event queue size has been reached: {MaxQueueSize}");
      }
      else
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
    }

    private int CountEvents()
    {
      int count = 0;
      foreach (var events in eventsDict)
      {
        count += events.Value.Count;
      }
      return count;
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

    private static int GetPriority(BotEvent botEvent, IBaseBot baseBot)
    {
      switch (botEvent)
      {
        case TickEvent tickEvent:
          return EventPriority.OnTick;
        case ScannedBotEvent scannedBotEvent:
          return EventPriority.OnScannedBot;
        case HitBotEvent hitBotEvent:
          return EventPriority.OnHitBot;
        case HitWallEvent hitWallEvent:
          return EventPriority.OnHitWall;
        case BulletFiredEvent bulletFiredEvent:
          return EventPriority.OnBulletFired;
        case BulletHitWallEvent bulletHitWallEvent:
          return EventPriority.OnBulletHitWall;
        case BulletHitBotEvent bulletHitBotEvent:
          if (bulletHitBotEvent.VictimId == baseBot.MyId)
            return EventPriority.OnHitByBullet;
          else
            return EventPriority.OnBulletHit;
        case BulletHitBulletEvent bulletHitBulletEvent:
          return EventPriority.OnBulletHitBullet;
        case DeathEvent deathEvent:
          if (deathEvent.VictimId == baseBot.MyId)
            return EventPriority.OnDeath;
          else
            return EventPriority.OnBotDeath;
        case SkippedTurnEvent skippedTurnEvent:
          return EventPriority.OnSkippedTurn;
        case CustomEvent customEvent:
          return EventPriority.OnCondition;
        case WonRoundEvent wonRoundEvent:
          return EventPriority.OnWonRound;
        default:
          throw new InvalidOperationException("Unhandled event type: " + botEvent);
      }
    }
  }
}