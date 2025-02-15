using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bot has won the round.
/// </summary>
public sealed class WonRoundEvent : BotEvent
{
    /// <summary>
    /// Indicates if this event is critical, and hence should not be removed from event queue when it gets old.
    /// This event is critical.
    /// </summary>
    /// <return><c>true</c></return>
    public override bool IsCritical => true;

    /// <summary>
    /// Initializes a new instance of the WonRoundEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    [JsonConstructor]
    public WonRoundEvent(int turnNumber) : base(turnNumber)
    {
    }
}