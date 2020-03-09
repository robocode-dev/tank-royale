using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when bot gets disconnected from server.
  /// </summary>
  public sealed class DisconnectedEvent
  {
    /// <summary>Indication if closing of the connection was initiated by the remote host.</summary>
    public bool Remote { get; }

    /// <summary>
    /// Initializes a new instance of the DisconnectedEvent class.
    /// </summary>
    /// <param name="remote">Indication if closing of the connection was initiated by the remote host.</param>
    [JsonConstructor]
    public DisconnectedEvent(bool remote) => Remote = remote;
  }
}