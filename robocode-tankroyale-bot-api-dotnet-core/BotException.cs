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
  }
}