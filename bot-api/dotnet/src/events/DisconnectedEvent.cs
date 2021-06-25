using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events
{
  /// <summary>
  /// Event occurring when bot gets disconnected from server.
  /// </summary>
  public sealed class DisconnectedEvent : ConnectionEvent
  {
    /// <summary>Indication if closing of the connection was initiated by the remote host.</summary>
    public bool Remote { get; }

    /// <summary>
    /// Initializes a new instance of the DisconnectedEvent class.
    /// </summary>
    /// <param name="serverUri">URI of the server.</param>
    /// <param name="remote">Indication if closing of the connection was initiated by the remote host.</param>
    [JsonConstructor]
    public DisconnectedEvent(System.Uri serverUri, bool remote) : base(serverUri) => Remote = remote;
  }
}