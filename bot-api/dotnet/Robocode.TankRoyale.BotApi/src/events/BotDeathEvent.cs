using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when another bot has died.
/// </summary>
public sealed class BotDeathEvent : BotEvent
{
    /// <summary>The id of the bot that has died.</summary>
    public int VictimId { get; }

    /// <summary>Initializes a new instance of the BotDeathEvent class.</summary>
    /// <param name="turnNumber">Turn number when the bot died.</param>
    /// <param name="victimId">Id of the bot that has died.</param>
    [JsonConstructor]
    public BotDeathEvent(int turnNumber, int victimId) : base(turnNumber) => VictimId = victimId;
}