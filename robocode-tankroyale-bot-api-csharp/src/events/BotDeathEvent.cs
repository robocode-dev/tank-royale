using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bot has died.
  /// </summary>
  public sealed class BotDeathEvent : Event
  {
    /// <summary>The id of the bot that has died.</summary>
    [Newtonsoft.Json.JsonProperty("victimId", Required = Newtonsoft.Json.Required.Always)]
    public int VictimId { get; }

    /// <summary>Initializes a new instance of the BotDeathEvent class.</summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">Id of the bot that has died.</param>
    [JsonConstructor]
    public BotDeathEvent(int turnNumber, int victimId) : base(turnNumber) => VictimId = victimId;
  }
}