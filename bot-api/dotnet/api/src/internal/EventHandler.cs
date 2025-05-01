using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Generic event handler for handling and dispatching events of type <typeparamref name="T"/>.
/// Events can be published to all subscribed listeners, which will be invoked in order of their priority.
/// Subscribers with higher priority are invoked before lower priority ones. This handler provides
/// thread-safety through synchronization and exception handling during event publication.
/// </summary>
/// <typeparam name="T">The type of event that this handler processes. Must implement the <see cref="IEvent"/> interface.</typeparam>
sealed class EventHandler<T> where T : IEvent
{
    /// <summary>
    /// Default priority value for subscribers.
    /// </summary>
    private const int DefaultPriority = 1;

    /// <summary>
    /// The lock object for thread synchronization.
    /// </summary>
    private readonly object _lock = new();

    /// <summary>
    /// A thread-safe list that holds subscriber entries with their respective priorities.
    /// This list is used to manage subscribers for an event handler, allowing subscribers
    /// to be added, sorted by priority, and invoked in the correct order based on priority.
    /// </summary>
    private readonly List<EntryWithPriority> _subscriberEntries = new();

    /// <summary>
    /// A set to quickly check for duplicate subscribers.
    /// </summary>
    private readonly HashSet<Subscriber<T>> _subscriberSet = new();

    /// <summary>
    /// Delegate that represents a method for handling events of a specific type.
    /// </summary>
    /// <typeparam name="TE">The type of the event data.</typeparam>
    /// <param name="eventData">The event data of type <typeparamref name="TE"/>.</param>
    public delegate void Subscriber<in TE>(TE eventData);

    /// <summary>
    /// Subscribes a new event handler to this EventHandler with a given priority.
    /// The subscribers are executed when an event is published.
    /// Higher priority values are executed before lower priority values.
    /// </summary>
    /// <param name="subscriber">The subscriber delegate that will handle the events. Must not be null.</param>
    /// <param name="priority">The priority of the subscriber; higher values indicate higher priority. Must be a non-negative value.</param>
    /// <exception cref="ArgumentNullException">Thrown if subscriber is null.</exception>
    /// <exception cref="ArgumentException">Thrown if priority is negative or if subscriber is already registered.</exception>
    public void Subscribe(Subscriber<T> subscriber, int priority = DefaultPriority)
    {
        if (subscriber == null) throw new ArgumentNullException(nameof(subscriber), "Subscriber cannot be null");
        if (priority < 0) throw new ArgumentException("Priority must be a non-negative value", nameof(priority));

        lock (_lock)
        {
            // Check for duplicate subscribers using HashSet for O(1) lookup
            if (!_subscriberSet.Add(subscriber))
            {
                throw new ArgumentException("Subscriber is already registered", nameof(subscriber));
            }

            // Add the new entry
            var newEntry = new EntryWithPriority(subscriber, priority);

            // Use binary search to find insertion point to maintain sorted order
            int index = _subscriberEntries.BinarySearch(newEntry, Comparer<EntryWithPriority>.Create((e1, e2) =>
                e2.Priority.CompareTo(e1.Priority)));

            if (index < 0)
            {
                _subscriberEntries.Insert(~index, newEntry); // Insert at the correct position
            }
            else
            {
                _subscriberEntries.Insert(index, newEntry); // Insert at the found position
            }
        }
    }

    /// <summary>
    /// Unsubscribes a subscriber from this event handler.
    /// If the subscriber is not subscribed, this method has no effect.
    /// </summary>
    /// <param name="subscriber">The subscriber to be removed from subscriptions.</param>
    /// <returns>true if the subscriber was found and removed, false otherwise.</returns>
    public bool Unsubscribe(Subscriber<T> subscriber)
    {
        if (subscriber == null) return false;

        lock (_lock)
        {
            bool removed = _subscriberEntries.RemoveAll(entry => ReferenceEquals(entry.Subscriber, subscriber)) > 0;
            if (removed)
            {
                _subscriberSet.Remove(subscriber);
            }
            return removed;
        }
    }

    /// <summary>
    /// Removes all subscribers from this event handler.
    /// </summary>
    public void Clear()
    {
        lock (_lock)
        {
            _subscriberEntries.Clear();
            _subscriberSet.Clear();
        }
    }

    /// <summary>
    /// Publishes an event, invoking all subscribed listeners in order of their priority.
    /// Subscribers with higher priority are invoked before those with lower priority.
    /// If a subscriber throws an exception, it is caught, allowing other
    /// subscribers to continue processing the event.
    ///
    /// Note: Changes to the subscriber list during event publication will not affect
    /// the current event being processed, but only future events.
    /// </summary>
    /// <param name="eventData">The event data to be published to the subscribers. Can be null, in which case subscribers must handle null values.</param>
    public void Publish(T eventData)
    {
        List<EntryWithPriority> entriesCopy;

        lock (_lock)
        {
            entriesCopy = new List<EntryWithPriority>(_subscriberEntries);
        }

        foreach (var entry in entriesCopy)
        {
            try
            {
                entry.Subscriber.Invoke(eventData);
            }
            catch (Exception)
            {
                // Catch the exception to allow other subscribers to process
                // No logging per requirements
            }
        }
    }

    /// <summary>
    /// Returns the number of subscribers currently registered with this event handler.
    /// </summary>
    /// <returns>The count of subscribers.</returns>
    public int GetSubscriberCount()
    {
        lock (_lock)
        {
            return _subscriberEntries.Count;
        }
    }

    /// <summary>
    /// Private class to store a subscriber together with its priority.
    /// </summary>
    private class EntryWithPriority : IComparable<EntryWithPriority>
    {
        /// <summary>
        /// Gets the priority of the subscriber. Higher values indicate higher priority.
        /// </summary>
        public int Priority { get; }

        /// <summary>
        /// Gets the subscriber delegate.
        /// </summary>
        public Subscriber<T> Subscriber { get; }

        /// <summary>
        /// Constructs a new entry with the specified subscriber and priority.
        /// </summary>
        /// <param name="subscriber">The subscriber delegate.</param>
        /// <param name="priority">The priority of the subscriber.</param>
        public EntryWithPriority(Subscriber<T> subscriber, int priority)
        {
            Priority = priority;
            Subscriber = subscriber;
        }

        /// <summary>
        /// Compares this instance with another EntryWithPriority instance.
        /// </summary>
        /// <param name="other">Another instance to compare with.</param>
        /// <returns>A value that indicates the relative order of the objects being compared.</returns>
        public int CompareTo(EntryWithPriority other)
        {
            // Note: We invert the comparison because higher priority should come first
            return other.Priority.CompareTo(Priority);
        }

        /// <summary>
        /// Returns the hash code for this instance.
        /// </summary>
        /// <returns>A hash code for the current object.</returns>
        public override int GetHashCode()
        {
            return HashCode.Combine(Priority, Subscriber);
        }

        /// <summary>
        /// Determines whether the specified object is equal to the current object.
        /// </summary>
        /// <param name="obj">The object to compare with the current object.</param>
        /// <returns>true if the specified object is equal to the current object; otherwise, false.</returns>
        public override bool Equals(object obj)
        {
            if (obj is EntryWithPriority other)
            {
                return Priority == other.Priority && ReferenceEquals(Subscriber, other.Subscriber);
            }
            return false;
        }
    }
}