using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when the bot has hit a wall.
/// </summary>
public sealed class HitWallEvent : BotEvent
{
    /// <summary>
    /// Initializes a new instance of the BotHitWallEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    [JsonConstructor]
    public HitWallEvent(int turnNumber) : base(turnNumber)
    {
    }
}