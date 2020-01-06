namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bullet has been fired from a bot.
  /// </summary>
  public sealed class BulletFiredEvent : Event
  {
    /// <summary>Bullet that was fired.<summary>
    public BulletState Bullet { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that was fired.</param>
    public BulletFiredEvent(int turnNumber, BulletState bullet) : base(turnNumber) => Bullet = bullet;
  }
}