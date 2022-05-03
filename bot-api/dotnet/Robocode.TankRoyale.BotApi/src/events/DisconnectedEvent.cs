using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when bot gets disconnected from server.
/// </summary>
public sealed class DisconnectedEvent : ConnectionEvent
{
    /// <summary>Indication if closing of the connection was initiated by the remote host.</summary>
    public bool Remote { get; }

    /// <summary>Status code that indicates the reason for closing the connection. Might be null.</summary>
    public int? StatusCode { get; }

    /// <summary>Message with the reason for closing the connection. Might be null.</summary>
    public string Reason { get; }

    /// <summary>
    /// Initializes a new instance of the DisconnectedEvent class.
    /// </summary>
    /// <param name="serverUri">URI of the server.</param>
    /// <param name="remote">Indication if closing of the connection was initiated by the remote host.</param>
    /// <param name="statusCode">Is a status code that indicates the reason for closing the connection.</param>
    /// <param name="reason">Is a message with the reason for closing the connection.</param>
    [JsonConstructor]
    public DisconnectedEvent(System.Uri serverUri, bool remote, int? statusCode, string reason) : base(serverUri)
    {
        Remote = remote;
        StatusCode = statusCode;
        Reason = reason;
    }
}