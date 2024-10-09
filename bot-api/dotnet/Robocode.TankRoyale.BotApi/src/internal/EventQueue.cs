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

    internal void DispatchEvents(int turnNumber)
    {
//        DumpEvents(); // for debugging purposes

        RemoveOldEvents(turnNumber);
        SortEvents();

        while (IsBotRunning)
        {
            BotEvent currentEvent = GetNextEvent();
            if (currentEvent == null) {
                break;
            }
            if (IsSameEvent(currentEvent))
            {
                if (IsInterruptible)
                {
                    SetInterruptible(currentEvent.GetType(), false); // clear interruptible flag

                    // We are already in an event handler, took action, and a new event was generated.
                    // So we want to break out of the old handler to process the new event here.
                    throw new InterruptEventHandlerException();
                }
                break;
            }

            int oldTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = GetPriority(currentEvent);
            currentTopEvent = currentEvent;

            try
            {
                HandleEvent(currentEvent, turnNumber);
            }
            catch (InterruptEventHandlerException)
            {
                // Expected when event handler is interrupted on purpose
            }
            finally
            {
                currentTopEventPriority = oldTopEventPriority;
            }
        }
    }

    private void RemoveOldEvents(int turnNumber)
    {
        foreach (var botEvent in events.Where(botEvent => IsOldAndNonCriticalEvent(botEvent, turnNumber)))
        {
            events = events.Remove(botEvent);
        }
    }

    private void SortEvents()
    {
        events = events.Sort(this);
    }

    private bool IsBotRunning => baseBotInternals.IsRunning;

    private BotEvent GetNextEvent()
    {
        if (events.IsEmpty) return null;
        var botEvent = events[0];
        events = events.Remove(botEvent);
        return botEvent;
    }

    private bool IsSameEvent(BotEvent botEvent) =>
        GetPriority(botEvent) == currentTopEventPriority;

    private int GetPriority(BotEvent botEvent)
    {
        return baseBotInternals.GetPriority(botEvent.GetType());
    }

    private void HandleEvent(BotEvent botEvent, int turnNumber)
    {
        try
        {
            if (IsNotOldOrIsCriticalEvent(botEvent, turnNumber))
            {
                botEventHandlers.Fire(botEvent);
            }
        }
        finally
        {
            SetInterruptible(botEvent.GetType(), false); // clear interruptible flag
        }
    }

    public int Compare(BotEvent botEvent1, BotEvent botEvent2)
    {
        // Critical must be placed before non-critical
        var diff = (botEvent2!.IsCritical ? 1 : 0) - (botEvent1!.IsCritical ? 1 : 0);
        if (diff != 0)
        {
            return diff;
        }

        // Lower (older) turn number must be placed before higher (newer) turn number
        diff = botEvent1!.TurnNumber - botEvent2!.TurnNumber;
        if (diff != 0)
        {
            return diff;
        }

        // Higher priority value must be placed before lower priority value
        return GetPriority(botEvent2) - GetPriority(botEvent1);
    }

    private static bool IsNotOldOrIsCriticalEvent(BotEvent botEvent, int turnNumber)
    {
        var isNotOld = botEvent.TurnNumber >= turnNumber - MaxEventAge;
        return isNotOld || botEvent.IsCritical;
    }

    private static bool IsOldAndNonCriticalEvent(BotEvent botEvent, int turnNumber)
    {
        var isOld = botEvent.TurnNumber < turnNumber - MaxEventAge;
        return isOld && !botEvent.IsCritical;
    }

    private void AddEvent(BotEvent botEvent)
    {
        if (events.Count <= MaxQueueSize)
        {
            events = events.Add(botEvent);
        }
        else
        {
            Console.Error.WriteLine("Maximum event queue size has been reached: " + MaxQueueSize);
        }
    }

    private void AddCustomEvents()
    {
        foreach (var condition in baseBotInternals.Conditions.Where(condition => condition.Test()))
        {
            AddEvent(new CustomEvent(baseBotInternals.CurrentTickOrThrow.TurnNumber, condition));
        }
    }

    private void DumpEvents()
    {
        string events = string.Join(", ", this.events.Select(e => $"{e.GetType().Name}({e.TurnNumber}) adlasdasd"));
        Console.WriteLine($"events: {events}");
    }
}