using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  public class EventManager<T>
  {
    public delegate void EventHandler(T eventData);

    private List<EventHandler> handlers = new List<EventHandler>();

    public void Add(EventHandler handler)
    {
      handlers.Add(handler);
    }

    public void InvokeAll(T eventData)
    {
      // Invoke one event handler at a time in the order they were added
      foreach (var handler in handlers)
      {
        handler.Invoke(eventData);
      }
    }
  }
}
