using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Manages event interruption settings for bot events.
/// 
/// This class is responsible for tracking which bot event types are marked as interruptible.
/// Events that are marked as interruptible can interrupt the normal execution event flow of the bot.
/// </summary>
static class EventInterruption
{
    /// <summary>
    /// Set containing all event classes that are currently marked as interruptible.
    /// </summary>
    private static readonly HashSet<System.Type> Interruptibles = new();

    /// <summary>
    /// Sets whether a specific event class should be interruptible or not.
    /// </summary>
    /// <param name="eventClass">The class of the event to configure</param>
    /// <param name="interruptible">true if the event should be interruptible; false otherwise</param>
    public static void SetInterruptible(System.Type eventClass, bool interruptible)
    {
        if (interruptible)
        {
            Interruptibles.Add(eventClass);
        }
        else
        {
            Interruptibles.Remove(eventClass);
        }
    }

    /// <summary>
    /// Checks if a specific event class is marked as interruptible.
    /// </summary>
    /// <param name="eventClass">The class of the event to check</param>
    /// <returns>true if the event is interruptible; false otherwise</returns>
    public static bool IsInterruptible(System.Type eventClass)
    {
        return Interruptibles.Contains(eventClass);
    }
}
