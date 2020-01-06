using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
  /// for a specific turn.
  /// </summary>
  public sealed class SkippedTurnEvent : Event
  {
    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    public SkippedTurnEvent(int turnNumber) : base(turnNumber)
    {
    }
  }
}