using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi.Util
{
  /// <summary>
  /// Event handler that is able to invoke events in the sequence, which they were added to the handler.
  /// </summary>
  /// <typeparam name="T"></typeparam>
  public class EventHandler<T>
  {
    /// <summary>
    /// Delegate method used for declaring events.
    /// </summary>
    /// <param name="eventData"></param>
    public delegate void Subscriber(T eventData);

    private List<EntryWithPriority> subscriberEntries = new List<EntryWithPriority>();

    /// <summary>
    /// Subscribe to events on the event handler.
    /// </summary>
    /// <param name="subscriber">Is the subscriber that receives notifications when an event is triggered.</param>
    /// <param name="priority">Is the priority of the event, where higher values means higher priorities.</param>
    public void Subscribe(Subscriber subscriber, int priority)
    {
      subscriberEntries.Add(new EntryWithPriority(subscriber, priority));
    }

    /// <summary>
    /// Subscribe to events on the event handler.
    /// </summary>
    /// <param name="subscriber">Is the subscriber that receives notifications when an event is triggered.</param>
    public void Subscribe(Subscriber subscriber)
    {
      Subscribe(subscriber, 1);
    }

    /// <summary>
    /// Publish an event, which invokes all subscribers in the same sequence as they were added to the handler.
    /// </summary>
    /// <param name="eventData">Is the data for the event.</param>
    public void Publish(T eventData)
    {
      subscriberEntries.Sort(compareByPriority);
      foreach (var entry in new List<EntryWithPriority>(subscriberEntries))
      {
        entry.subscriber.Invoke(eventData);
      }
    }

    static int compareByPriority(EntryWithPriority e1, EntryWithPriority e2)
    {
      return e2.priority - e1.priority;
    }

    class EntryWithPriority
    {
      public readonly int priority; // Lower values means lower priority
      public readonly Subscriber subscriber;

      public EntryWithPriority(Subscriber subscriber, int priority)
      {
        this.subscriber = subscriber;
        this.priority = priority;
      }
    }
  }
}