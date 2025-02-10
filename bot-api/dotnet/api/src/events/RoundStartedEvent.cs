using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a round has just started.
/// </summary>
public sealed class RoundStartedEvent : IEvent
{
    /// <summary>The round number.</summary>
    public int RoundNumber { get; }

    /// <summary>
    /// Initializes a new instance of the RoundStartedEvent class.
    /// </summary>
    /// <param name="roundNumber">The round number</param>
    [JsonConstructor]
    public RoundStartedEvent(int roundNumber) => RoundNumber = roundNumber;
}