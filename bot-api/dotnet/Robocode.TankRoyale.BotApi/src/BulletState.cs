using System.Drawing;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Represents the state of a bullet that has been fired by a bot.
/// </summary>
public sealed class BulletState
{
    /// <summary>
    /// Unique id of the bullet.
    /// </summary>
    /// <value>The unique id of the bullet.</value>
    public int BulletId { get; }

    /// <summary>Id of the owner bot that fired the bullet.</summary>
    /// <value>The id of the owner bot that fired the bullet.</value>
    public int OwnerId { get; }

    /// <summary>Bullet firepower level.</summary>
    /// <value>The bullet firepower level.</value>
    public double Power { get; }

    /// <summary>X coordinate of the bullet.</summary>
    /// <value>The X coordinate of the bullet.</value>
    public double X { get; }

    /// <summary>Y coordinate of the bullet.</summary>
    /// <value>The Y coordinate of the bullet.</value>
    public double Y { get; }

    /// <summary>Direction of the bullet in degrees.</summary>
    /// <value>The direction of the bullet in degrees.</value>
    public double Direction { get; }

    /// <summary>Speed measured of the bullet in units per turn.</summary>
    /// <value>The speed measured of the bullet in units per turn.</value>
    public double Speed => 20 - 3 * Power;

    /// <summary>
    /// color of the bullet.
    /// </summary>
    /// <value>The color of the bullet.</value>
    public Color? Color { get; }

    /// <summary>
    /// Initializes a new instance of the BulletState class.
    /// </summary>
    /// <param name="bulletId">Unique id of the bullet.</param>
    /// <param name="ownerId">Id of the bot that fired the bullet.</param>
    /// <param name="power">Bullet firepower level.</param>
    /// <param name="x">X coordinate of the bullet.</param>
    /// <param name="y">Y coordinate of the bullet.</param>
    /// <param name="direction">Direction in degrees.</param>
    /// <param name="color">color of the bullet.</param>
    public BulletState(int bulletId, int ownerId, double power, double x, double y, double direction, Color? color)
    {
        BulletId = bulletId;
        OwnerId = ownerId;
        Power = power;
        X = x;
        Y = y;
        Direction = direction;
        Color = color;
    }
}