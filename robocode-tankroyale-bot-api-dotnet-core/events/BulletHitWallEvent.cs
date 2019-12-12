namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when a bullet has hit a wall.
  /// </summary>
  public sealed class BulletHitWallEvent : Event
  {
    /// <summary>Bullet that has hit a wall.<summary>
    BulletState Bullet { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that has hit a wall.</param>
    /// <returns></returns>
    public BulletHitWallEvent(int turnNumber, BulletState bullet) : base(turnNumber) => Bullet = bullet;
  }
}