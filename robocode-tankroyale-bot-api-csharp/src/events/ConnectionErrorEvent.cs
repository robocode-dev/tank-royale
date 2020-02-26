using System;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when a connection error occurs.
  /// </summary>
  public sealed class ConnectionErrorEvent
  {
    /// <summary>The exception causing the error.</summary>
    public Exception Exception { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="exception">The exception causing the error.</param>
    [JsonConstructor]
    public ConnectionErrorEvent(Exception exception) => Exception = exception;
  }
}