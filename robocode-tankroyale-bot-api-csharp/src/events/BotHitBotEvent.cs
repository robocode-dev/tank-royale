using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bot has collided with another bot.
  /// </summary>
  public sealed class BotHitBotEvent : Event
  {
    /// <summary>The id of the bot that got hit.</summary>
    [Newtonsoft.Json.JsonProperty("victimId", Required = Newtonsoft.Json.Required.Always)]
    public int VictimId { get; }

    /// <summary>ID of the bot that hit another bot.</summary>
    public int BotId { get; }

    /// <summary>Remaining energy level of the victim bot.</summary>
    public double Energy { get; }

    /// <summary>X coordinate of victim bot.</summary>
    public double X { get; }

    /// <summary>Y coordinate of victim bot.</summary>
    public double Y { get; }

    /// <summary>Flag specifying, if the victim bot got rammed.</summary>
    public bool Rammed { get; }

    /// <summary>
    /// Initializes a new instance of the BotHitBotEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">Id of the victim bot that got hit.</param>
    /// <param name="botId">Id of the bot that hit another bot.</param>
    /// <param name="energy">Remaining energy level of the victim bot.</param>
    /// <param name="x">X coordinate of victim bot.</param>
    /// <param name="y">Y coordinate of victim bot.</param>
    /// <param name="rammed">Flag specifying, if the victim bot got rammed.</param>
    [JsonConstructor]
    public BotHitBotEvent(int turnNumber, int victimId, int botId, double energy, double x, double y, bool rammed) : base(turnNumber) =>
      (VictimId, BotId, Energy, X, Y, Rammed) = (victimId, botId, energy, x, y, rammed);
  }
}