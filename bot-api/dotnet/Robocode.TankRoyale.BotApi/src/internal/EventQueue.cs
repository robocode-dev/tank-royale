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

    private readonly ISet<Type> interruptibles = new HashSet<Type>();

    internal EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers)
    {
        this.baseBotInternals = baseBotInternals;
        this.botEventHandlers = botEventHandlers;
    }

    internal void Clear()
    {
        events = events.Clear();
        baseBotInternals.Conditions.Clear(); // conditions might be added in the bots Run() method each round
        currentTopEvent = null;
        currentTopEventPriority = MinValue;
    }

    internal IList<BotEvent> Events => new List<BotEvent>(events);

    internal void ClearEvents()
    {
        events = events.Clear();
    }

    internal void SetInterruptible(bool interruptible)
    {
        SetInterruptible(currentTopEvent.GetType(), interruptible);
    }

    internal void SetInterruptible(Type eventType, bool interruptible)
    {
        if (interruptible)
            interruptibles.Add(eventType);
        else
            interruptibles.Remove(eventType);
    }

    private bool IsInterruptible => interruptibles.Contains(currentTopEvent.GetType());

    internal void AddEventsFromTick(TickEvent tickEvent)
    {
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

        while (baseBotInternals.IsRunning && !events.IsEmpty)
        {
            var botEvent = events[0];
            var priority = GetPriority(botEvent);

            if (priority < currentTopEventPriority)
                break;

            // Same event?
            if (priority == currentTopEventPriority)
            {
                if (currentTopEventPriority > MinValue && IsInterruptible)
                {
                    SetInterruptible(botEvent.GetType(), false);
                    // The current event handler must be interrupted (by throwing an InterruptEventHandlerException)
                    throw new InterruptEventHandlerException();
                }
                break; // Ignore same event occurring again
            }

            var oldTopEventPriority = currentTopEventPriority;
            currentTopEventPriority = priority;
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

    private int GetPriority(BotEvent botEvent)
    {
        return baseBotInternals.GetPriority(botEvent.GetType());
    }

    public int Compare(BotEvent e1, BotEvent e2)
    {
        // Critical must be placed before non-critical
        var diff = (e2!.IsCritical ? 1 : 0) - (e1!.IsCritical ? 1 : 0);
        if (diff != 0)
        {
            return diff;
        }
        // Lower (older) turn number must be placed before higher (newer) turn number
        diff = e1!.TurnNumber - e2!.TurnNumber;
        if (diff != 0)
        {
            return diff;
        }
        // Lower priority value (means higher priority!) must be placed before higher priority values
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
}