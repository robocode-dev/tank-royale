using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when game has just ended.
  /// </summary>
  public sealed class GameEndedEvent : IMessage
  {
    /// <summary>Number of rounds played.</summary>
    int NumberOfRounds { get; }

    /// <summary>Results of the battle.</summary>
    List<BotResults> Results { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">ID of the bot that has died.</param>
    public GameEndedEvent(int numberOfRounds, List<BotResults> results) : base() =>
      (NumberOfRounds, Results) = (numberOfRounds, results);
  }
}