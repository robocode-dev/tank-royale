using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bullet has been fired from a bot.
/// </summary>
public sealed class BulletFiredEvent : BotEvent
{
    /// <summary>Bullet that was fired.</summary>
    public BulletState Bullet { get; }

    /// <summary>
    /// Initializes a new instance of the BulletFiredEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that was fired.</param>
    [JsonConstructor]
    public BulletFiredEvent(int turnNumber, BulletState bullet) : base(turnNumber) => Bullet = bullet;
}