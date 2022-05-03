using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when bot gets connected to server.
/// </summary>
public sealed class ConnectedEvent : ConnectionEvent
{
    /// <summary>
    /// Initializes a new instance of the ConnectedEvent class.
    /// </summary>
    /// <param name="serverUri">URI of the server.</param>
    [JsonConstructor]
    public ConnectedEvent(System.Uri serverUri) : base(serverUri)
    {
    }
}