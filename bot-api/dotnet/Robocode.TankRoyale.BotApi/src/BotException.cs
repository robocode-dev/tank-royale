using System;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Represents errors that occur with bot execution.
/// </summary>
public class BotException : Exception
{
    /// <summary>
    /// Initializes a new instance of the BotException class with a specified error message.
    /// </summary>
    /// <param name="message">The message that describes the error.</param>
    public BotException(string message) : base(message)
    {
    }

    /// <summary>
    /// Initializes a new instance of the BotException class with a specified error
    /// message and a reference to the inner exception that is the cause of this exception.
    /// </summary>
    /// <param name="message">The error message that explains the reason for the exception.</param>
    /// <param name="innerException">The exception that is the cause of the current exception.</param>
    public BotException(string message, Exception innerException) : base(message, innerException)
    {
    }
}