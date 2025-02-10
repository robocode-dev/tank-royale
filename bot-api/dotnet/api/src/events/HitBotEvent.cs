using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bot has collided with another bot.
/// </summary>
public sealed class HitBotEvent : BotEvent
{
    /// <summary>The id of the other bot that your bot has collided with.</summary>
    public int VictimId { get; }

    /// <summary>Remaining energy level of the victim bot.</summary>
    public double Energy { get; }

    /// <summary>X coordinate of victim bot.</summary>
    public double X { get; }

    /// <summary>Y coordinate of victim bot.</summary>
    public double Y { get; }

    /// <summary>Flag specifying, if the victim bot got rammed.</summary>
    public bool IsRammed { get; }

    /// <summary>
    /// Initializes a new instance of the BotHitBotEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">Id of the victim bot that got hit.</param>
    /// <param name="energy">Remaining energy level of the victim bot.</param>
    /// <param name="x">X coordinate of victim bot.</param>
    /// <param name="y">Y coordinate of victim bot.</param>
    /// <param name="isRammed">Flag specifying, if the victim bot got rammed.</param>
    [JsonConstructor]
    public HitBotEvent(int turnNumber, int victimId, double energy, double x, double y, bool isRammed) :
        base(turnNumber) =>
        (VictimId, Energy, X, Y, IsRammed) = (victimId, energy, x, y, isRammed);
}