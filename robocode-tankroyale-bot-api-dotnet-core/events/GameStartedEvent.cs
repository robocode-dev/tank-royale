using System.Collections.Generic;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Event occurring when game has just started.
  /// </summary>
  public sealed class GameStartedEvent : IMessage
  {
    /// <summary>The ID used for identifying your bot in the current battle.</summary>
    int MyId { get; }

    /// <summary>The game setup for the battle just started.</summary>
    GameSetup GameSetup { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="myId">The ID used for identifying your bot in the current battle.</param>
    /// <param name="gameSetup">The game setup for the battle just started.</param>
    public GameStartedEvent(int myId, GameSetup gameSetup) : base() =>
      (MyId, GameSetup) = (myId, gameSetup);
  }
}