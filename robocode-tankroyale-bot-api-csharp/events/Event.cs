using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring during a battle.
  /// </summary>
  public class Event : IMessage
  {
    /// <summary>Turn number.</summary>
    public int TurnNumber { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number</param>
    [JsonConstructor]
    public Event(int turnNumber) => TurnNumber = turnNumber;
  }
}