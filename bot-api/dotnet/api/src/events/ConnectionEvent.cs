using System;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Base class of all connection events.
/// </summary>
public abstract class ConnectionEvent : IEvent
{
    /// <summary>URI of the server.</summary>
    public Uri ServerUri { get; }

    /// <summary>
    /// Initializes a new instance of the ConnectionEvent class.
    /// </summary>
    /// <param name="serverUri">URI of the server.</param>
    protected ConnectionEvent(Uri serverUri) => ServerUri = serverUri;
}