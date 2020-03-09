using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bullet has hit a bot.
  /// </summary>
  public sealed class BulletHitBotEvent : Event
  {
    /// <summary>The id of the bot that got hit.</summary>
    [Newtonsoft.Json.JsonProperty("victimId", Required = Newtonsoft.Json.Required.Always)]
    public int VictimId { get; }

    /// <summary>Bullet that hit the bot.</summary>
    public BulletState Bullet { get; }

    /// <summary>Damage inflicted by the bullet.</summary>
    public double Damage { get; }

    /// <summary>Remaining energy level of the bot that got hit.</summary>
    public double Energy { get; }

    /// <summary>
    /// Initializes a new instance of the BulletHitBotEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that hit the bot.</param>
    /// <param name="damage">Damage inflicted by the bullet.</param>
    /// <param name="energy">Remaining energy level of the bot that got hit.</param>
    [JsonConstructor]
    public BulletHitBotEvent(int turnNumber, int victimId, BulletState bullet, double damage, double energy) : base(turnNumber) =>
      (VictimId, Bullet, Damage, Energy) = (victimId, bullet, damage, energy);
  }
}