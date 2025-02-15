using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bullet has hit a wall.
/// </summary>
public sealed class BulletHitWallEvent : BotEvent
{
    /// <summary>Bullet that has hit a wall.</summary>
    public BulletState Bullet { get; }

    /// <summary>
    /// Initializes a new instance of the BulletHitWallEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that has hit a wall.</param>
    [JsonConstructor]
    public BulletHitWallEvent(int turnNumber, BulletState bullet) : base(turnNumber) => Bullet = bullet;
}