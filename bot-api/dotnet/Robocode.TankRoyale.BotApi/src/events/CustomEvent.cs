using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// A custom event occurring when a condition has been met.
/// </summary>
public sealed class CustomEvent : BotEvent
{
    ///<summary>Condition that was met to trigger this custom event.</summary>
    public Condition Condition { get; }

    /// <summary>
    /// Initializes a new instance of the CustomEvent class. 
    /// </summary>
    /// <param name="turnNumber">Is the turn number when the condition was met.</param>
    /// <param name="condition">Is the condition that has been met.</param>
    [JsonConstructor]
    public CustomEvent(int turnNumber, Condition condition) : base(turnNumber) => Condition = condition;
}