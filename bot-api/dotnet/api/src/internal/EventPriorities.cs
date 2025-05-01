using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Manages priorities for bot events in the Tank Royale game.
/// <para>
/// This class maintains a registry of event priorities that determine the order
/// in which events are processed by the bot API.
/// </para>
/// </summary>
static class EventPriorities
{
    private static readonly Dictionary<Type, int> EventPrioritiesDict = InitializeEventPriorities();

    /// <summary>
    /// Initializes the default event priorities dictionary.
    /// </summary>
    /// <returns>A dictionary containing default priority values for all supported event types</returns>
    private static Dictionary<Type, int> InitializeEventPriorities()
    {
        var priorities = new Dictionary<Type, int>
        {
            { typeof(WonRoundEvent), DefaultEventPriority.WonRound },
            { typeof(SkippedTurnEvent), DefaultEventPriority.SkippedTurn },
            { typeof(TickEvent), DefaultEventPriority.Tick },
            { typeof(CustomEvent), DefaultEventPriority.Custom },
            { typeof(TeamMessageEvent), DefaultEventPriority.TeamMessage },
            { typeof(BotDeathEvent), DefaultEventPriority.BotDeath },
            { typeof(BulletHitWallEvent), DefaultEventPriority.BulletHitWall },
            { typeof(BulletHitBulletEvent), DefaultEventPriority.BulletHitBullet },
            { typeof(BulletHitBotEvent), DefaultEventPriority.BulletHitBot },
            { typeof(BulletFiredEvent), DefaultEventPriority.BulletFired },
            { typeof(HitByBulletEvent), DefaultEventPriority.HitByBullet },
            { typeof(HitWallEvent), DefaultEventPriority.HitWall },
            { typeof(HitBotEvent), DefaultEventPriority.HitBot },
            { typeof(ScannedBotEvent), DefaultEventPriority.ScannedBot },
            { typeof(DeathEvent), DefaultEventPriority.Death }
        };
        return priorities;
    }

    /// <summary>
    /// Sets the priority for a specific event type.
    /// </summary>
    /// <param name="eventType">The event type to set priority for</param>
    /// <param name="priority">The priority value to assign</param>
    /// <exception cref="ArgumentNullException">Thrown when <paramref name="eventType"/> is null</exception>
    /// <exception cref="ArgumentException">Thrown when <paramref name="eventType"/> does not inherit from <see cref="BotEvent"/></exception>
    public static void SetPriority(Type eventType, int priority)
    {
        if (eventType == null)
        {
            throw new ArgumentNullException(nameof(eventType), "Event type cannot be null");
        }

        if (!typeof(BotEvent).IsAssignableFrom(eventType))
        {
            throw new ArgumentException($"Event type {eventType.FullName} is not a BotEvent");
        }

        EventPrioritiesDict[eventType] = priority;
    }

    /// <summary>
    /// Gets the priority for a specific event type.
    /// </summary>
    /// <param name="eventType">The event type to get priority for</param>
    /// <returns>The priority value for the specified event type</returns>
    /// <exception cref="ArgumentNullException">Thrown when <paramref name="eventType"/> is null</exception>
    /// <exception cref="ArgumentException">Thrown when <paramref name="eventType"/> does not inherit from <see cref="BotEvent"/></exception>
    /// <exception cref="InvalidOperationException">Thrown when no priority is defined for the event type</exception>
    public static int GetPriority(Type eventType)
    {
        if (eventType == null)
        {
            throw new ArgumentNullException(nameof(eventType), "Event type cannot be null");
        }

        if (!typeof(BotEvent).IsAssignableFrom(eventType))
        {
            throw new ArgumentException($"Event type {eventType.FullName} is not a BotEvent");
        }

        if (!EventPrioritiesDict.TryGetValue(eventType, out var priority))
        {
            throw new InvalidOperationException($"Could not get event priority for the type: {eventType.Name}");
        }

        return priority;
    }
}
