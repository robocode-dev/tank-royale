using JetBrains.Annotations;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Bot event occurring during a battle.
/// </summary>
[PublicAPI]
public abstract class BotEvent : IEvent
{
    /// <summary>The turn number when this event occurred.</summary>
    public int TurnNumber { get; }

    /// <summary>
    /// Determines whether the event is critical.
    /// By default, events are not critical, but subclasses can override this
    /// to provide event-specific criticality logic.
    /// </summary>
    /// <returns>
    /// <c>false</c> by default.
    /// </returns>
    public virtual bool IsCritical => false;

    protected BotEvent(int turnNumber) => TurnNumber = turnNumber;
}