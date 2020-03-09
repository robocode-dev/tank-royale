namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring during a battle.
  /// </summary>
  public abstract class Event : IMessage
  {
    /// <summary>Turn number.</summary>
    public int TurnNumber { get; }

    /// <summary>
    /// Initializes a new instance of the Event class.
    /// </summary>
    /// <param name="turnNumber">Turn number</param>
    public Event(int turnNumber) => TurnNumber = turnNumber;
  }
}