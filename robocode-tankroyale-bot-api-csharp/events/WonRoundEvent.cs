using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bot has won the round.
  /// </summary>
  public sealed class WonRoundEvent : Event
  {
    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    public WonRoundEvent(int turnNumber) : base(turnNumber)
    {
    }
  }
}