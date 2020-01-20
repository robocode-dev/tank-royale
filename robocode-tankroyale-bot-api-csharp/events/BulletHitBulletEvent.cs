using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bullet has collided with another bullet.
  /// </summary>
  public sealed class BulletHitBulletEvent : Event
  {
    /// <summary>Bullet that hit another bullet.<summary>
    public BulletState Bullet { get; }

    /// <summary>The other bullet that was hit by the bullet.<summary>
    public BulletState HitBullet { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that hit another bullet.</param>
    /// <param name="hitBullet">The other bullet that was hit by the bullet.</param>
    /// <returns></returns>
    [JsonConstructor]
    public BulletHitBulletEvent(int turnNumber, BulletState bullet, BulletState hitBullet) : base(turnNumber) =>
      (Bullet, HitBullet) = (bullet, hitBullet);
  }
}