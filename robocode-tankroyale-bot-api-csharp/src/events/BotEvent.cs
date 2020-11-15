namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Bot event occurring during a battle.
  /// </summary>
  public abstract class BotEvent : IEvent
  {
    /// <summary>Turn number when the event occurred.</summary>
    public int TurnNumber { get; }

    ///<summary>
    ///Initializes a new instance of the Event class.
    ///</summary>
    ///<param name="turnNumber">Is the turn number when the event occurred.</param>
    protected BotEvent(int turnNumber) => TurnNumber = turnNumber;
  }
}