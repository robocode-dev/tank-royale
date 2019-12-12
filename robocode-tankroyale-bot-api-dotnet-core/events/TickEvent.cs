using System.Collections.Generic;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring whenever a new turn in a round has started.
  /// </summary>
  public sealed class TickEvent : Event
  {
    /// <summary>Current round number.</summary>
    int RoundNumber { get; }

    /// <summary>Current state of this bot.</summary>
    BotState BotState { get; }

    /// <summary>Current state of the bullets fired by this bot.</summary>
    ICollection<BulletState> BulletStates { get; }

    /// <summary>Current state of the bullets fired by this bot.</summary>
    ICollection<Event> Events { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    public TickEvent(int turnNumber, int roundNumber, BotState botState,
      ICollection<BulletState> bulletStates, ICollection<Event> events) : base(turnNumber) =>
      (RoundNumber, BotState, BulletStates, Events) =
      (roundNumber, botState, bulletStates, events);
  }
}