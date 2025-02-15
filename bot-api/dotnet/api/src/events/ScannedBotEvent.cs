using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bot has scanned another bot.
/// </summary>
public sealed class ScannedBotEvent : BotEvent
{
    /// <summary>Id of the bot did the scanning.</summary>
    public int ScannedByBotId { get; }

    /// <summary>Id of the bot that was scanned.</summary>
    public int ScannedBotId { get; }

    /// <summary>Energy level of the scanned bot.</summary>
    public double Energy { get; }

    /// <summary>X coordinate of the scanned bot.</summary>
    public double X { get; }

    /// <summary>Y coordinate of the scanned bot.</summary>
    public double Y { get; }

    /// <summary>Direction in degrees of the scanned bot.</summary>
    public double Direction { get; }

    /// <summary>Speed measured in units per turn of the scanned bot.</summary>
    public double Speed { get; }

    /// <summary>
    /// Initializes a new instance of the ScannedBotEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="scannedByBotId">Id of the bot did the scanning.</param>
    /// <param name="scannedBotId">Id of the bot that was scanned.</param>
    /// <param name="energy">Energy level of the scanned bot.</param>
    /// <param name="x">X coordinate of the scanned bot.</param>
    /// <param name="y">Y coordinate of the scanned bot.</param>
    /// <param name="direction">Direction in degrees of the scanned bot.</param>
    /// <param name="speed">Speed measured in units per turn of the scanned bot.</param>
    [JsonConstructor]
    public ScannedBotEvent(int turnNumber, int scannedByBotId, int scannedBotId, double energy,
        double x, double y, double direction, double speed) : base(turnNumber) =>
        (ScannedByBotId, ScannedBotId, Energy, X, Y, Direction, Speed) =
        (scannedByBotId, scannedBotId, energy, x, y, direction, speed);
}