using System.Collections.Generic;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring whenever a new turn in a round has started.
/// </summary>
public sealed class TickEvent : BotEvent
{
    /// <summary>Current round number.</summary>
    public int RoundNumber { get; }

    /// <summary>Current state of this bot.</summary>
    public BotState BotState { get; }

    /// <summary>Current state of the bullets fired by this bot.</summary>
    public IEnumerable<BulletState> BulletStates { get; }

    /// <summary>Events that occurred for the bot within the turn.</summary>
    public IEnumerable<BotEvent> Events { get; }

    /// <summary>
    /// Initializes a new instance of the TickEvent class.
    /// </summary>
    /// <param name="turnNumber">Current turn number in the battle.</param>
    /// <param name="roundNumber">Current round number in the battle.</param>
    /// <param name="botState">Current state of this bot.</param>
    /// <param name="bulletStates">Current state of the bullets fired by this bot.</param>
    /// <param name="events">Events occurring in the turn relevant for this bot.</param>
    [JsonConstructor]
    public TickEvent(int turnNumber, int roundNumber, BotState botState, IEnumerable<BulletState> bulletStates,
        IEnumerable<BotEvent> events) : base(turnNumber) =>
        (RoundNumber, BotState, BulletStates, Events) =
        (roundNumber, botState, bulletStates, events);
}