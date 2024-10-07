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

    internal void DispatchEvents(int turnNumber)
    {
        RemoveOldEvents(turnNumber);
        SortEvents();

        while (IsBotRunning)
        {
            var botEvent = GetNextEvent();
            if (botEvent == null || IsSameEvent(botEvent))
                break;

            var priority = GetPriority(botEvent);
            var originalTopEventPriority = currentTopEventPriority;

            currentTopEventPriority = priority;
            currentTopEvent = botEvent;

            try {
                HandleEvent(botEvent, turnNumber);
            } catch (InterruptEventHandlerException) {
                // Expected when event handler is being interrupted
            } finally {
                currentTopEventPriority = originalTopEventPriority;
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
        GetPriority(botEvent) == currentTopEventPriority && (currentTopEventPriority > MinValue && IsInterruptible);

    private int GetPriority(BotEvent botEvent)
    {
        return baseBotInternals.GetPriority(botEvent.GetType());
    }

    private void HandleEvent(BotEvent botEvent, int turnNumber) {
        if (IsNotOldOrIsCriticalEvent(botEvent, turnNumber)) {
            botEventHandlers.Fire(botEvent);
        }
        var isInterruptible = IsInterruptible;

        SetInterruptible(botEvent.GetType(), false); // clear interruptible flag

        if (isInterruptible)
            throw new InterruptEventHandlerException();
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
        var isNotOld = botEvent.TurnNumber + MaxEventAge >= turnNumber;
        var isCritical = botEvent.IsCritical;
        return isNotOld || isCritical;
    }

    private static bool IsOldAndNonCriticalEvent(BotEvent botEvent, int turnNumber)
    {
        var isOld = botEvent.TurnNumber + MaxEventAge < turnNumber;
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
            AddEvent(new CustomEvent(baseBotInternals.CurrentTickOrThrow.TurnNumber, condition));
        }
    }
}