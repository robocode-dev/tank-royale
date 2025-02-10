using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when your bot has died.
/// </summary>
public sealed class DeathEvent : BotEvent
{
    /// <summary>
    /// Indicates if this event is critical, and hence should not be removed from event queue when it gets old.
    /// This event is critical.
    /// </summary>
    /// <return><c>true</c></return>
    public override bool IsCritical => true;

    /// <summary>Initializes a new instance of the DeathEvent class.</summary>
    /// <param name="turnNumber">Turn number when your bot died.</param>
    [JsonConstructor]
    public DeathEvent(int turnNumber) : base(turnNumber)
    {
    }
}