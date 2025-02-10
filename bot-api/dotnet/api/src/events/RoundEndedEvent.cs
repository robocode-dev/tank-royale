using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a round has just ended.
/// </summary>
public sealed class RoundEndedEvent : IEvent
{
    /// <summary>The round number.</summary>
    public int RoundNumber { get; }

    /// <summary>The turn number.</summary>
    public int TurnNumber { get; }

    /// <summary>The accumulated bot results at the end of the round.</summary>
    public BotResults Results { get; }

    /// <summary>
    /// Initializes a new instance of the RoundEndedEvent class.
    /// </summary>
    /// <param name="roundNumber">The round number</param>
    /// <param name="turnNumber">The turn number.</param>
    /// <param name="results">The accumulated bot results at the end of the round.</param>
    [JsonConstructor]
    public RoundEndedEvent(int roundNumber, int turnNumber, BotResults results) =>
        (RoundNumber, TurnNumber, Results) = (roundNumber, turnNumber, results);
}