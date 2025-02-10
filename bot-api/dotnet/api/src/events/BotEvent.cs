namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Bot event occurring during a battle.
/// </summary>
public abstract class BotEvent : IEvent
{
    public int TurnNumber { get; }
    public virtual bool IsCritical => false;
    protected BotEvent(int turnNumber) => TurnNumber = turnNumber;
}