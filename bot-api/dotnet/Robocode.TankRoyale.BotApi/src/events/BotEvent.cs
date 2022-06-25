namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Bot event occurring during a battle.
/// </summary>
public abstract class BotEvent : IEvent
{
    /// <summary>Turn number when the event occurred.</summary>
    public int TurnNumber { get; }

    /// <summary>
    /// Indicates if this event is critical, and hence should not be removed from event queue when it gets old.
    /// </summary>
    /// <return>
    /// <c>true</c> if this event is critical; <c>false</c> otherwise. Default is <c>false</c>.
    /// </return>
    public virtual bool IsCritical => false;

    ///<summary>
    /// Initializes a new instance of the Event class.
    ///</summary>
    ///<param name="turnNumber">Is the turn number when the event occurred.</param>
    protected BotEvent(int turnNumber) => TurnNumber = turnNumber;
}