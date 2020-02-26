using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event Manager that is able to invoke event in the sequence, which they were added to the manager.
  /// </summary>
  /// <typeparam name="T"></typeparam>
  public class EventManager<T>
  {
    /// <summary>
    /// Delegate method used for declaring events.
    /// </summary>
    /// <param name="eventData"></param>
    public delegate void EventHandler(T eventData);

    private List<EventHandler> handlers = new List<EventHandler>();

    /// <summary>
    /// Adds an event handler.
    /// </summary>
    /// <param name="handler">Is the event handler to add.</param>
    public void Add(EventHandler handler)
    {
      handlers.Add(handler);
    }

    /// <summary>
    /// Invoke all event handlers in the same sequence as they were added.
    /// </summary>
    /// <param name="eventData"></param>
    public void InvokeAll(T eventData)
    {
      foreach (var handler in handlers)
      {
        handler.Invoke(eventData);
      }
    }
  }
}
