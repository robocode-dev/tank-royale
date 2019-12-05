namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when a bullet has hit a bot.
  /// </summary>
  public class BulletHitBotEvent : Event
  {
    /// <summary>ID of the victim bot that got hit.<summary>
    int VictimId { get; }

    /// <summary>Bullet that hit the bot.<summary>
    BulletState Bullet { get; }

    /// <summary>Damage inflicted by the bullet.<summary>
    double Damage { get; }

    /// <summary>Remaining energy level of the bot that got hit.<summary>
    double Energy { get; }

    /// <summary>
    /// Constrcutor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="bullet">Bullet that hit the bot.</param>
    /// <param name="damage">Damage inflicted by the bullet.</param>
    /// <param name="energy">Remaining energy level of the bot that got hit.</param>
    public BulletHitBotEvent(int turnNumber, int victimId, BulletState bullet, double damage, double energy) : base(turnNumber) =>
      (VictimId, Bullet, Damage, Energy) = (victimId, bullet, damage, energy);
  }
}