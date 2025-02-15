using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Event handler that is able to invoke events in the sequence, which they were added to the handler.
/// </summary>
/// <typeparam name="T"></typeparam>
public class EventHandler<T> where T : IEvent
{
    /// <summary>
    /// Delegate method used for declaring events.
    /// </summary>
    /// <param name="eventData"></param>
    public delegate void Subscriber(T eventData);

    private readonly List<EntryWithPriority> subscriberEntries = new();

    /// <summary>
    /// Subscribe to events on the event handler.
    /// </summary>
    /// <param name="subscriber">Is the subscriber that receives notifications when an event is triggered.</param>
    /// <param name="priority">Is the priority of the event, where higher values means higher priorities.</param>
    public void Subscribe(Subscriber subscriber, int priority = 1)
    {
        subscriberEntries.Add(new EntryWithPriority(subscriber, priority));
    }

    /// <summary>
    /// Publish an event, which invokes all subscribers in the same sequence as they were added to the handler.
    /// </summary>
    /// <param name="eventData">Is the data for the event.</param>
    public void Publish(T eventData)
    {
        subscriberEntries.Sort(CompareByPriority);
        foreach (var entry in new List<EntryWithPriority>(subscriberEntries))
        {
            entry.Subscriber.Invoke(eventData);
        }
    }

    private static int CompareByPriority(EntryWithPriority e1, EntryWithPriority e2)
    {
        return e2.Priority - e1.Priority;
    }

    private class EntryWithPriority
    {
        public int Priority { get; }
        public Subscriber Subscriber { get; } // Lower values means lower priority

        public EntryWithPriority(Subscriber subscriber, int priority)
        {
            Priority = priority;
            Subscriber = subscriber;
        }
    }
}