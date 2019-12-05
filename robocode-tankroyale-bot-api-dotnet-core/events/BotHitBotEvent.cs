namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when a bot has collided with another bot.
  /// </summary>
  public class BotHitBotEvent : Event
  {
    /// <summary>ID of the victim bot that got hit.</summary>
    int VictimId { get; }

    /// <summary>ID of the bot that hit another bot.</summary>
    int BotId { get; }

    /// <summary>Remaining energy level of the victim bot.</summary>
    double Energy { get; }

    /// <summary>X coordinate of victim bot.</summary>
    double X { get; }

    /// <summary>Y coordinate of victim bot.</summary>
    double Y { get; }

    /// <summary>Flag specifying, if the victim bot got rammed.</summary>
    bool Rammed { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">ID of the victim bot that got hit.</param>
    /// <param name="botId">ID of the bot that hit another bot.</param>
    /// <param name="energy">Remaining energy level of the victim bot.</param>
    /// <param name="x">X coordinate of victim bot.</param>
    /// <param name="y">Y coordinate of victim bot.</param>
    /// <param name="rammed">Flag specifying, if the victim bot got rammed.</param>
    public BotHitBotEvent(int turnNumber, int victimId, int botId, double energy, double x, double y, bool rammed) : base(turnNumber) =>
      (VictimId, BotId, Energy, X, Y, Rammed) = (victimId, botId, energy, x, y, rammed);
  }
}