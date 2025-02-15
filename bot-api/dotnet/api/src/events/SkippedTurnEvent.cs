using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
/// for a specific turn.
/// </summary>
public sealed class SkippedTurnEvent : BotEvent
{
    /// <summary>
    /// Indicates if this event is critical, and hence should not be removed from event queue when it gets old.
    /// This event is critical.
    /// </summary>
    /// <return><c>true</c></return>
    public override bool IsCritical => true;

    /// <summary>
    /// Initializes a new instance of the SkippedTurnEvent class.
    /// </summary>
    /// <param name="turnNumber">The turn number which was skipped.</param>
    [JsonConstructor]
    public SkippedTurnEvent(int turnNumber) : base(turnNumber)
    {
    }
}