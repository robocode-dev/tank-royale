using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Event occurring when a bot has won the round.
  /// </summary>
  public sealed class WonRoundEvent : BotEvent
  {
    /// <summary>
    /// Initializes a new instance of the WonRoundEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    [JsonConstructor]
    public WonRoundEvent(int turnNumber) : base(turnNumber) { }
  }
}