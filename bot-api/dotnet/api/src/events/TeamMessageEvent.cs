using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when a bot has received a message from a teammate.
/// </summary>
public sealed class TeamMessageEvent : BotEvent
{
    /// <summary>
    /// The serialized message that was received.
    /// </summary>
    public object Message { get; }

    /// <summary>
    /// The ID of the teammate that sent the message.
    /// </summary>
    public int SenderId { get; }

    /// <summary>
    /// Initializes a new instance of the TeamMessageEvent class.
    /// </summary>
    /// <param name="turnNumber">Turn number.</param>
    /// <param name="message">The message that was received.</param>
    /// <param name="senderId">The id of the teammate that sent the message.</param>
    [JsonConstructor]
    public TeamMessageEvent(int turnNumber, object message, int senderId) : base(turnNumber) =>
        (Message, SenderId) = (message, senderId);
}