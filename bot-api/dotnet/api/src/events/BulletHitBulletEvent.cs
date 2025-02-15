using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bullet has collided with another bullet.
/// </summary>
public sealed class BulletHitBulletEvent : BotEvent
{
    /// <summary>Bullet that hit another bullet.</summary>
    public BulletState Bullet { get; }

    /// <summary>The other bullet that was hit by the bullet.</summary>
    public BulletState HitBullet { get; }

    /// <summary>
    /// Initializes a new instance of the BulletHitBulletEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that hit another bullet.</param>
    /// <param name="hitBullet">The other bullet that was hit by the bullet.</param>
    [JsonConstructor]
    public BulletHitBulletEvent(int turnNumber, BulletState bullet, BulletState hitBullet) : base(turnNumber) =>
        (Bullet, HitBullet) = (bullet, hitBullet);
}