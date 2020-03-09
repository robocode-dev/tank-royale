using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when the bot has hit a wall.
  /// </summary>
  public sealed class BotHitWallEvent : Event
  {
    /// <summary>The id of the bot that hit the wall.</summary>
    [Newtonsoft.Json.JsonProperty("victimId", Required = Newtonsoft.Json.Required.Always)]
    public int VictimId { get; }

    /// <summary>
    /// Initializes a new instance of the BotHitWallEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">Id of the victim bot that hit the wall.</param>
    [JsonConstructor]
    public BotHitWallEvent(int turnNumber, int victimId) : base(turnNumber) => VictimId = victimId;
  }
}