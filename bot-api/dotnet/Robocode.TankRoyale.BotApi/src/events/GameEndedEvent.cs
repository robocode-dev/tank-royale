using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when game has just ended.
/// </summary>
public sealed class GameEndedEvent : IEvent
{
    /// <summary>Number of rounds played.</summary>
    public int NumberOfRounds { get; }

    /// <summary>Results of the battle.</summary>
    public BotResults Results { get; }

    /// <summary>
    /// Initializes a new instance of the GameEndedEvent class.
    /// </summary>
    /// <param name="numberOfRounds">Number of rounds played.</param>
    /// <param name="results">Results of the battle.</param>
    [JsonConstructor]
    public GameEndedEvent(int numberOfRounds, BotResults results) =>
        (NumberOfRounds, Results) = (numberOfRounds, results);
}