using System.Collections.Generic;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when a bot has scanned another bot.
  /// </summary>
  public sealed class ScannedBotEvent : Event
  {
    /// <summary>ID of the bot did the scanning.</summary>
    int ScannedByBotId { get; }

    /// <summary>ID of the bot that was scanned.</summary>
    int ScannedBotId { get; }

    /// <summary>Energy level of the scanned bot.</summary>
    double Energy { get; }

    /// <summary>X coordinate of the scanned bot.</summary>
    double X { get; }

    /// <summary>Y coordinate of the scanned bot.</summary>
    double Y { get; }

    /// <summary>Direction in degrees of the scanned bot.</summary>
    double Direction { get; }

    /// <summary>Speed measured in pixels per turn of the scanned bot.</summary>
    double Speed { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="scannedByBotId">ID of the bot did the scanning.</param>
    /// <param name="scannedBotId">ID of the bot that was scanned.</param>
    /// <param name="energy">Energy level of the scanned bot.</param>
    /// <param name="x">X coordinate of the scanned bot.</param>
    /// <param name="y">Y coordinate of the scanned bot.</param>
    /// <param name="direction">Direction in degrees of the scanned bot.</param>
    /// <param name="speed">Speed measured in pixels per turn of the scanned bot.</param>
    public ScannedBotEvent(int turnNumber, int scannedByBotId, int scannedBotId, double energy,
     double x, double y, double direction, double speed) : base(turnNumber) =>
      (ScannedByBotId, ScannedBotId, Energy, X, Y, Direction, Speed) =
      (scannedByBotId, scannedBotId, energy, x, y, direction, speed);
  }
}