namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a bot has died.
  /// </summary>
  public sealed class BotDeathEvent : Event
  {
    /// <summary>ID of the bot that has died.<summary>
    int VictimId { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="victimId">ID of the bot that has died.</param>
    public BotDeathEvent(int turnNumber, int victimId) : base(turnNumber) => VictimId = victimId;
  }
}