using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
  /// for a specific turn.
  /// </summary>
  public sealed class SkippedTurnEvent : BotEvent
  {
    /// <summary>
    /// Initializes a new instance of the SkippedTurnEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    [JsonConstructor]
    public SkippedTurnEvent(int turnNumber) : base(turnNumber) { }
  }
}