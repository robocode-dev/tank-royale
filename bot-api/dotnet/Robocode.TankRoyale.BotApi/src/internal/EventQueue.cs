using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using Robocode.TankRoyale.BotApi.Events;
using static System.Int32;

namespace Robocode.TankRoyale.BotApi.Internal;

internal sealed class EventQueue : IComparer<BotEvent>
{
    private const int MaxQueueSize = 256;
    private const int MaxEventAge = 2;

    private readonly BaseBotInternals baseBotInternals;
    private readonly BotEventHandlers botEventHandlers;

    private ImmutableList<BotEvent> events = ImmutableList<BotEvent>.Empty;

    private BotEvent currentTopEvent;
    private int currentTopEventPriority;

    private ISet<Type> interruptibles = new HashSet<Type>();

    private bool isDisabled;

    internal EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers)
    {
        this.baseBotInternals = baseBotInternals;
        this.botEventHandlers = botEventHandlers;
    }

    public void Clear()
    {
        events = events.Clear();
        baseBotInternals.Conditions.Clear(); // conditions might be added in the bots Run() method each round
        currentTopEvent = null;
        currentTopEventPriority = MinValue;
        isDisabled = false;
    }

    public void Disable()
    {
        isDisabled = true;
    }

    public void SetInterruptible(bool interruptible)
    {
        SetInterruptible(currentTopEvent.GetType(), interruptible);
    }

    public void SetInterruptible(Type eventType, bool interruptible)
    {
        if (interruptible)
            interruptibles.Add(eventType);
        else
            interruptibles.Remove(eventType);
    }

    private bool IsInterruptible => interruptibles.Contains(currentTopEvent.GetType());

    internal void AddEventsFromTick(TickEvent tickEvent)
    {
        if (isDisabled) return;

        AddEvent(tickEvent);
        foreach (var botEvent in tickEvent.Events)
        {
            AddEvent(botEvent);
        }

        AddCustomEvents();
    }

    internal void DispatchEvents(int currentTurn)
    {
        RemoveOldEvents(currentTurn);

        SortEvents();

        while (events.Count > 0)
        {
            var botEvent = events[0];
            var eventPriority = GetPriority(botEvent);

            Console.WriteLine(currentTurn + ": " + botEvent.GetType());

            if (eventPriority < currentTopEventPriority)
                return; // Exit when event priority is lower than the current event being processed

            // Same event?
            if (eventPriority == currentTopEventPriority)
            {
                if (!IsInterruptible)
                    // Ignore same event occurring again, when not interruptible
                    return;

                SetInterruptible(botEvent.GetType(), false);
                // The current event handler must be interrupted (by throwing an InterruptEventHandlerException)
                throw new InterruptEventHandlerException();
            }

            var oldTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = eventPriority;
            currentTopEvent = botEvent;

            events = events.Remove(botEvent);

            try
            {
                if (IsNotOldOrCriticalEvent(botEvent, currentTurn))
                    botEventHandlers.Fire(botEvent);

                SetInterruptible(botEvent.GetType(), false);
            }
            catch (InterruptEventHandlerException)
            {
                // Expected when event handler is being interrupted
            }
            finally
            {
                currentTopEventPriority = oldTopEventPriority;
            }
        }
    }

    private void RemoveOldEvents(int currentTurn)
    {
        foreach (var botEvent in events.Where(botEvent => IsOldAndNonCriticalEvent(botEvent, currentTurn)))
        {
            events = events.Remove(botEvent);
        }
    }

    private void SortEvents()
    {
        events = events.Sort(this);
    }

    public int Compare(BotEvent e1, BotEvent e2)
    {
        var timeDiff = e2.TurnNumber - e1.TurnNumber;
        if (timeDiff != 0)
        {
            return timeDiff;
        }

        return GetPriority(e1) - GetPriority(e2);
    }

    private static bool IsNotOldOrCriticalEvent(BotEvent botEvent, int currentTurn)
    {
        var isNotOld = botEvent.TurnNumber + MaxEventAge >= currentTurn;
        var isCritical = botEvent.IsCritical;
        return isNotOld || isCritical;
    }

    private static bool IsOldAndNonCriticalEvent(BotEvent botEvent, int currentTurn)
    {
        var isOld = botEvent.TurnNumber + MaxEventAge < currentTurn;
        var isNonCritical = !botEvent.IsCritical;
        return isOld && isNonCritical;
    }

    private void AddEvent(BotEvent botEvent)
    {
        if (events.Count > MaxQueueSize)
        {
            Console.Error.WriteLine("Maximum event queue size has been reached: " + MaxQueueSize);
        }
        else
        {
            events = events.Add(botEvent);
        }
    }

    private void AddCustomEvents()
    {
        foreach (var condition in baseBotInternals.Conditions.Where(condition => condition.Test()))
        {
            AddEvent(new CustomEvent(baseBotInternals.CurrentTick.TurnNumber, condition));
        }
    }

    private int GetPriority(BotEvent botEvent)
    {
        return botEvent switch
        {
            TickEvent _ => EventPriority.OnTick,
            ScannedBotEvent _ => EventPriority.OnScannedBot,
            HitBotEvent _ => EventPriority.OnHitBot,
            HitWallEvent _ => EventPriority.OnHitWall,
            BulletFiredEvent _ => EventPriority.OnBulletFired,
            BulletHitWallEvent _ => EventPriority.OnBulletHitWall,
            BulletHitBotEvent bulletHitBotEvent => bulletHitBotEvent.VictimId == baseBotInternals.MyId
                ? EventPriority.OnHitByBullet
                : EventPriority.OnBulletHit,
            BulletHitBulletEvent _ => EventPriority.OnBulletHitBullet,
            DeathEvent deathEvent => deathEvent.VictimId == baseBotInternals.MyId
                ? EventPriority.OnDeath
                : EventPriority.OnBotDeath,
            SkippedTurnEvent _ => EventPriority.OnSkippedTurn,
            CustomEvent _ => EventPriority.OnCondition,
            WonRoundEvent _ => EventPriority.OnWonRound,
            _ => throw new InvalidOperationException("Unhandled event type: " + botEvent)
        };
    }
}