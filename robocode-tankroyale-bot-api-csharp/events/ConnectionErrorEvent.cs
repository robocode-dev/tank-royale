using System;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when a connection error occurs.
  /// </summary>
  public sealed class ConnectionErrorEvent
  {
    /// <summary>The exception causing the error.</summary>
    Exception Exception { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="exception">The exception causing the error.</param>
    public ConnectionErrorEvent(Exception exception) => Exception = exception;
  }
}