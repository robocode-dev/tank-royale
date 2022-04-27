using System;
using System.Collections;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
    internal sealed class EventQueue
    {
        private const int MaxQueueSize = 256;
        private const int MaxEventAge = 2;

        private readonly BaseBotInternals baseBotInternals;
        private readonly BotEventHandlers botEventHandlers;

        private readonly IDictionary<int, ArrayList> eventsDict = new ConcurrentDictionary<int, ArrayList>();

        private BotEvent currentEvent;

        private ISet<Type> interruptibles = new HashSet<Type>();

        private bool isDisabled;

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
            isDisabled = false;
        }

        public void Disable()
        {
            isDisabled = true;
        }

        public void SetInterruptible(bool interruptable)
        {
            SetInterruptible(currentEvent.GetType(), interruptable);
        }

        public void SetInterruptible(Type eventType, bool interruptable)
        {
            if (interruptable)
                interruptibles.Add(eventType);
            else
                interruptibles.Remove(eventType);
        }

        private bool IsInterruptible => interruptibles.Contains(currentEvent.GetType());

        internal void AddEventsFromTick(TickEvent tickEvent, IBaseBot baseBot)
        {
            if (isDisabled) return;

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
            var sortedDict = new SortedDictionary<int, ArrayList>(eventsDict);

            foreach (var events in sortedDict.Values)
            {
                for (var i = 0; i < events.Count; i++)
                {
                    try
                    {
                        var evt = (BotEvent)events[i];

                        // Inside same event handler?
                        if (currentEvent != null && evt?.GetType() == currentEvent.GetType())
                        {
                            if (IsInterruptible)
                            {
                                SetInterruptible(evt.GetType(), false);
                                throw new InterruptEventHandlerException();
                            }

                            return; // ignore same event occurring again, when not interruptible
                        }

                        // Dispatch event
                        try
                        {
                            currentEvent = evt;
                            events.RemoveAt(i); // remove event prior to handling it
                            botEventHandlers.Fire(evt);

                            SetInterruptible(evt?.GetType(), false);
                        }
                        catch (InterruptEventHandlerException)
                        {
                        }
                        finally
                        {
                            currentEvent = null;
                        }
                    }
                    catch (ArgumentOutOfRangeException)
                    {
                    }
                }
            }
        }

        private void RemoveOldEvents(int currentTurn)
        {
            foreach (var events in eventsDict.Values)
            {
                for (var i = 0; i < events.Count; i++)
                {
                    try
                    {
                        var evt = (BotEvent)events[i];
                        if (evt is { IsCritical: false } && IsOldEvent(evt, currentTurn))
                            events.RemoveAt(i);
                    }
                    catch (ArgumentOutOfRangeException)
                    {
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
                var priority = GetPriority(botEvent, baseBot);

                eventsDict.TryGetValue(priority, out var events);
                if (events == null)
                {
                    events = ArrayList.Synchronized(new ArrayList());
                    eventsDict.Add(priority, events);
                }

                events.Add(botEvent);
            }
        }

        private int CountEvents()
        {
            return eventsDict.Values.Sum(events => events.Count);
        }

        private void AddCustomEvents(IBaseBot baseBot)
        {
            foreach (var condition in baseBotInternals.Conditions.Where(condition => condition.Test()))
            {
                AddEvent(new CustomEvent(baseBotInternals.CurrentTick.TurnNumber, condition), baseBot);
            }
        }

        private static int GetPriority(BotEvent botEvent, IBaseBot baseBot)
        {
            return botEvent switch
            {
                TickEvent _ => EventPriority.OnTick,
                ScannedBotEvent _ => EventPriority.OnScannedBot,
                HitBotEvent _ => EventPriority.OnHitBot,
                HitWallEvent _ => EventPriority.OnHitWall,
                BulletFiredEvent _ => EventPriority.OnBulletFired,
                BulletHitWallEvent _ => EventPriority.OnBulletHitWall,
                BulletHitBotEvent bulletHitBotEvent => bulletHitBotEvent.VictimId == baseBot.MyId
                    ? EventPriority.OnHitByBullet
                    : EventPriority.OnBulletHit,
                BulletHitBulletEvent _ => EventPriority.OnBulletHitBullet,
                DeathEvent deathEvent => deathEvent.VictimId == baseBot.MyId
                    ? EventPriority.OnDeath
                    : EventPriority.OnBotDeath,
                SkippedTurnEvent _ => EventPriority.OnSkippedTurn,
                CustomEvent _ => EventPriority.OnCondition,
                WonRoundEvent _ => EventPriority.OnWonRound,
                _ => throw new InvalidOperationException("Unhandled event type: " + botEvent)
            };
        }
    }
}