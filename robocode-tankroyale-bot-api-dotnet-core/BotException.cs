using System;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Bot exception.
  /// </summary>
  public class BotException : Exception
  {
    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="message">Error message</param>
    public BotException(string message) : base(message)
    {
    }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="message">Error message</param>
    /// </summary>
    public BotException(string message, Exception innerException) : base(message, innerException)
    {
    }
  }
}