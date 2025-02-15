using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bullet has hit your bot.
/// </summary>
public sealed class HitByBulletEvent : BotEvent
{
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
    public HitByBulletEvent(int turnNumber, BulletState bullet, double damage, double energy) : base(turnNumber) =>
        (Bullet, Damage, Energy) = (bullet, damage, energy);
}