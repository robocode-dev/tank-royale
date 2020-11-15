namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Event occurring during a battle.
  /// </summary>
  public abstract class Event : IEvent
  {
    /// <summary>Turn number when the event occurred.</summary>
    public int TurnNumber { get; }

    /// <summary>
    /// Initializes a new instance of the Event class.
    /// </summary>
    /// <param name="turnNumber">Turn number when the event occurred.</param>
    public Event(int turnNumber) => TurnNumber = turnNumber;
  }
}