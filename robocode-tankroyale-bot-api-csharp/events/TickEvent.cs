using System.Collections.Generic;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring whenever a new turn in a round has started.
  /// </summary>
  public sealed class TickEvent : Event
  {
    /// <summary>Current round number.</summary>
    public int RoundNumber { get; }

    /// <summary>Current state of this bot.</summary>
    public BotState BotState { get; }

    /// <summary>Current state of the bullets fired by this bot.</summary>
    public ICollection<BulletState> BulletStates { get; }

    /// <summary>Current state of the bullets fired by this bot.</summary>
    public ICollection<Event> Events { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    [JsonConstructor]
    public TickEvent(int turnNumber, int roundNumber, BotState botState,
      ICollection<BulletState> bulletStates, ICollection<Event> events) : base(turnNumber) =>
      (RoundNumber, BotState, BulletStates, Events) =
      (roundNumber, botState, bulletStates, events);
  }
}