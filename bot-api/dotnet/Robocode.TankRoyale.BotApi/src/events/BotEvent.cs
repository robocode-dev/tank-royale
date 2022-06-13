using System;
using System.Collections.Generic;

using static Robocode.TankRoyale.BotApi.Events.DefaultEventPriority;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Bot event occurring during a battle.
/// </summary>
public abstract class BotEvent : IEvent
{
    private static readonly IDictionary<Type, int> eventPriorities = new Dictionary<Type, int>();

    static BotEvent()
    {
        eventPriorities[typeof(TickEvent)] = OnTick;
        eventPriorities[typeof(WonRoundEvent)] = OnWonRound;
        eventPriorities[typeof(SkippedTurnEvent)] = OnSkippedTurn;
        eventPriorities[typeof(CustomEvent)] = OnCondition;
        eventPriorities[typeof(BotDeathEvent)] = OnBotDeath;
        eventPriorities[typeof(BulletFiredEvent)] = OnBulletFired;
        eventPriorities[typeof(BulletHitWallEvent)] = OnBulletHitWall;
        eventPriorities[typeof(BulletHitBulletEvent)] = OnBulletHitBullet;
        eventPriorities[typeof(BulletHitBotEvent)] = OnBulletHit;
        eventPriorities[typeof(HitByBulletEvent)] = OnHitByBullet;
        eventPriorities[typeof(HitWallEvent)] = OnHitWall;
        eventPriorities[typeof(HitBotEvent)] = OnHitBot;
        eventPriorities[typeof(ScannedBotEvent)] = OnScannedBot;
        eventPriorities[typeof(DeathEvent)] = OnDeath;
    }

    /// <summary>Turn number when the event occurred.</summary>
    public int TurnNumber { get; }

    /// <summary>
    /// Event priority for the event class which determines which event types (classes) that must be handled
    /// before others. Events with higher priorities will be handled before events with lower priorities.
    ///
    /// Note that you should normally not need to change the event priority.
    /// </summary>
    /// <value>The priority. Typically, a positive number from 1 to 150. The greater value, the higher priority.</value>
    public int Priority
    {
        get
        {
            var type = GetType();
            if (!eventPriorities.ContainsKey(type))
                throw new InvalidOperationException("Could not get event priority for the type: " + type);
            return eventPriorities[type];
        }
        set => eventPriorities[GetType()] = value;
    }

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