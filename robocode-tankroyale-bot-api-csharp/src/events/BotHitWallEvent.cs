using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when the bot has hit a wall.
  /// </summary>
  public sealed class BotHitWallEvent : Event
  {
    /// <summary>
    /// Initializes a new instance of the BotHitWallEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    [JsonConstructor]
    public BotHitWallEvent(int turnNumber) : base(turnNumber) { }
  }
}