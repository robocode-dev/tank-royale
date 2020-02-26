namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Bullet state.
  /// </summary>
  public sealed class BulletState
  {
    /// <summary>
    /// ID of the bullet.
    /// </summary>
    int BulletId { get; }

    /// <summary>ID of the bot that fired the bullet.</summary>
    int OwnerId { get; }

    /// <summary>Bullet firepower level.</summary>
    double Power { get; }

    /// <summary>X coordinate.</summary>
    double X { get; }

    /// <summary>Y coordinate.</summary>
    double Y { get; }

    /// <summary>Direction in degrees.</summary>
    double Direction { get; }

    /// <summary>Speed measured in pixels per turn.</summary>
    double Speed { get; }

    /// <summary>
    /// Constructor
    /// </summary>
    /// <param name="bulletId">ID of the bullet.</param>
    /// <param name="ownerId">ID of the bot that fired the bullet.</param>
    /// <param name="power">Bullet firepower level.</param>
    /// <param name="x">X coordinate.</param>
    /// <param name="y">Y coordinate.</param>
    /// <param name="direction">Direction in degrees.</param>
    /// <param name="speed">Speed measured in pixels per turn.</param>
    public BulletState(int bulletId, int ownerId, double power, double x, double y, double direction, double speed)
    {
      BulletId = bulletId;
      OwnerId = ownerId;
      Power = power;
      X = x;
      Y = y;
      Direction = direction;
      Speed = speed;
    }
  }
}