using System;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a connection error occurs.
/// </summary>
public sealed class ConnectionErrorEvent : ConnectionEvent
{
    /// <summary>The exception causing the error.</summary>
    public Exception Exception { get; }

    /// <summary>
    /// Initializes a new instance of the ConnectionErrorEvent class.
    /// </summary>
    /// <param name="serverUri">URI of the server.</param>
    /// <param name="exception">Exception causing the error.</param>
    [JsonConstructor]
    public ConnectionErrorEvent(Uri serverUri, Exception exception) : base(serverUri) => Exception = exception;
}