using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Event occurring when bot gets connected to server.
  /// </summary>
  public sealed class ConnectedEvent
  {
    // Currently empty. Preserved for future

    /// <summary>
    /// Initializes a new instance of the ConnectedEvent class.
    /// </summary>
    [JsonConstructor]
    public ConnectedEvent() { }
  }
}