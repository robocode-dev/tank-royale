/// <summary>
/// Game setup retrieved when game is started.
/// </summary>
public sealed class GameSetup
{
  /// <summary>
  /// Game type, e.g. "melee".
  /// </summary>
  string GameType { get; }

  /// <summary>
  /// Width of the arena measured in pixels.
  /// </summary>
  int ArenaWidth { get; }

  /// <summary>
  /// Height of the arena measured in pixels.
  /// </summary>
  int ArenaHeight { get; }

  /// <summary>
  /// Number of rounds in a battle.
  /// </summary>
  int NumberOfRounds { get; }

  /// <summary>
  /// Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun is able to fire.
  /// </summary>
  double GunCoolingRate { get; }

  /// <summary>
  /// Maximum number of inactive turns allowed, where a bot does not take any action before it is
  /// zapped by the game.
  /// </summary>
  int MaxInactivityTurns { get; }

  /// <summary>
  /// Timeout in milliseconds for sending intent after having received 'tick' message.
  /// </summary>
  int TurnTimeout { get; }

  /// <summary>
  /// Time limit in milliseconds for sending ready message after having received 'new battle' message.
  /// </summary>
  int ReadyTimeout { get; }
}
