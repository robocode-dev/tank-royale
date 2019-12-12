namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when bot gets disconnected from server.
  /// </summary>
  public sealed class DisconnectedEvent
  {
    /// <summary>Indication if closing of the connection was initiated by the remote host.</summary>
    bool Remote { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="remote">Indication if closing of the connection was initiated by the remote host.</param>
    public DisconnectedEvent(bool remote) => Remote = remote;
  }
}