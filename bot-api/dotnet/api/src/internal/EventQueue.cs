using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using Robocode.TankRoyale.BotApi.Events;
using static System.Int32;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// A queue for managing and dispatching bot events in the Tank Royale game.
/// </summary>
/// <remarks>
/// This class is responsible for:
/// <list type="bullet">
///     <item><description>Maintaining a prioritized queue of bot events</description></item>
///     <item><description>Managing event lifecycle and dispatching</description></item>
///     <item><description>Handling event interruptions and priorities</description></item>
///     <item><description>Processing custom events</description></item>
///     <item><description>Cleaning up old events</description></item>
/// </list>
/// <para>
/// The queue has a maximum size of <see cref="MaxQueueSize"/> events and maintains
/// events for up to <see cref="MaxEventAge"/> turns before they are considered old
/// and removed (unless they are critical events).
/// </para>
/// </remarks>
sealed class EventQueue : IComparer<BotEvent>
{
    private const int MaxQueueSize = 256;
    private const int MaxEventAge = 2;

    private readonly BaseBotInternals _baseBotInternals;
    private readonly BotEventHandlers _botEventHandlers;

    private readonly List<BotEvent> _events = new();

    private BotEvent _currentTopEvent;
    private int _currentTopEventPriority;

    /// <summary>
    /// Constructs an EventQueue with the specified bot internals and event handlers.
    /// </summary>
    /// <param name="baseBotInternals">The base bot internals instance</param>
    /// <param name="botEventHandlers">The bot event handlers instance</param>
    internal EventQueue(BaseBotInternals baseBotInternals, BotEventHandlers botEventHandlers)
    {
        _baseBotInternals = baseBotInternals;
        _botEventHandlers = botEventHandlers;
    }

    /// <summary>
    /// Clears all events from the queue and resets the event priority.
    /// </summary>
    internal void Clear()
    {
        ClearEvents();
        _baseBotInternals.ClearConditions(); // conditions might be added in the bots Run() method each round
        _currentTopEventPriority = MinValue;
    }

    /// <summary>
    /// Returns a copy of all events for the specified turn number after removing old events.
    /// </summary>
    /// <param name="turnNumber">The current turn number</param>
    /// <returns>A list of bot events</returns>
    internal IList<BotEvent> Events(int turnNumber)
    {
        lock (_events)
        {
            RemoveOldEvents(turnNumber);
            return new List<BotEvent>(_events);
        }
    }

    /// <summary>
    /// Removes all events from the queue.
    /// </summary>
    internal void ClearEvents()
    {
        lock (_events)
        {
            _events.Clear();
        }
    }

    /// <summary>
    /// Sets whether the current event can be interrupted.
    /// </summary>
    /// <param name="interruptible">True if the event can be interrupted, false otherwise</param>
    internal void SetCurrentEventInterruptible(bool interruptible)
    {
        EventInterruption.SetInterruptible(_currentTopEvent.GetType(), interruptible);
    }

    private bool IsCurrentEventInterruptible => EventInterruption.IsInterruptible(_currentTopEvent.GetType());

    /// <summary>
    /// Adds events from a tick event to the queue, including custom events.
    /// </summary>
    /// <param name="tickEvent">The tick event containing events to add</param>
    internal void AddEventsFromTick(TickEvent tickEvent)
    {
        AddEvent(tickEvent);
        foreach (var botEvent in tickEvent.Events)
        {
            AddEvent(botEvent);
        }

        AddCustomEvents();
    }

    /// <summary>
    /// Dispatches events for the specified turn number, processing them according to their priorities.
    /// </summary>
    /// <param name="turnNumber">The current turn number</param>
    internal void DispatchEvents(int turnNumber)
    {
//        DumpEvents(); // for debugging purposes

        RemoveOldEvents(turnNumber);
        SortEvents();

        while (IsBotRunning)
        {
            BotEvent currentEvent = GetNextEvent();
            if (currentEvent == null)
            {
                break;
            }

            if (IsSameEvent(currentEvent))
            {
                if (IsCurrentEventInterruptible)
                {
                    EventInterruption.SetInterruptible(_currentTopEvent.GetType(), false); // clear interruptible flag

                    // We are already in an event handler, took action, and a new event was generated.
                    // So we want to break out of the old handler to process the new event here.
                    throw new ThreadInterruptedException();
                }

                break;
            }

            int oldTopEventPriority = _currentTopEventPriority;

            _currentTopEventPriority = GetPriority(currentEvent);
            _currentTopEvent = currentEvent;

            lock (_events)
            {
                _events.Remove(currentEvent);
            }

            try
            {
                Dispatch(currentEvent, turnNumber);
            }
            catch (ThreadInterruptedException)
            {
                // Expected when event handler is interrupted on purpose
            }
            finally
            {
                _currentTopEventPriority = oldTopEventPriority;
            }
        }
    }

    private void RemoveOldEvents(int turnNumber)
    {
        lock (_events)
        {
            foreach (var botEvent in _events.Where(botEvent => IsOldAndNonCriticalEvent(botEvent, turnNumber)).ToList())
            {
                _events.Remove(botEvent);
            }
        }
    }

    private void SortEvents()
    {
        lock (_events)
        {
            _events.Sort(this);
        }
    }

    private bool IsBotRunning => _baseBotInternals.IsRunning;

    private BotEvent GetNextEvent()
    {
        lock (_events)
        {
            if (_events.Count == 0) return null;
            var botEvent = _events[0];
            _events.Remove(botEvent);
            return botEvent;
        }
    }

    private bool IsSameEvent(BotEvent botEvent) =>
        GetPriority(botEvent) == _currentTopEventPriority;

    private int GetPriority(BotEvent botEvent)
    {
        return EventPriorities.GetPriority(botEvent.GetType());
    }

    private void Dispatch(BotEvent botEvent, int turnNumber)
    {
        try
        {
            if (IsNotOldOrIsCriticalEvent(botEvent, turnNumber))
            {
                _botEventHandlers.FireEvent(botEvent);
            }
        }
        finally
        {
            EventInterruption.SetInterruptible(botEvent.GetType(), false);
        }
    }

    /// <summary>
    /// Compares two BotEvent objects to determine their order in the event queue.
    /// </summary>
    /// <remarks>
    /// The comparison is based on three factors (in order of precedence):
    /// <list type="number">
    ///     <item><description>Critical events come before non-critical events</description></item>
    ///     <item><description>Older turn number (lower value) comes before newer turn number (higher value)</description></item>
    ///     <item><description>Higher priority value comes before lower priority value</description></item>
    /// </list>
    /// </remarks>
    /// <param name="botEvent1">The first BotEvent to compare</param>
    /// <param name="botEvent2">The second BotEvent to compare</param>
    /// <returns>
    /// A negative value if botEvent1 should come before botEvent2,
    /// a positive value if botEvent2 should come before botEvent1,
    /// or zero if they are equivalent in priority
    /// </returns>
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
        lock (_events)
        {
            if (_events.Count <= MaxQueueSize)
            {
                _events.Add(botEvent);
            }
            else
            {
                Console.Error.WriteLine("Maximum event queue size has been reached: " + MaxQueueSize);
            }
        }
    }

    private void AddCustomEvents()
    {
        foreach (var condition in _baseBotInternals.Conditions.Where(condition => condition.Test()))
        {
            AddEvent(new CustomEvent(_baseBotInternals.CurrentTickOrThrow.TurnNumber, condition));
        }
    }

    // Used for debugging purposes
    private void DumpEvents(int turnNumber)
    {
        lock (_events)
        {
            var eventsString = string.Join(", ", _events.Select(e => $"{e.GetType().Name}({e.TurnNumber})"));
            Console.WriteLine($"{turnNumber} events: {eventsString}");
        }
    }
}