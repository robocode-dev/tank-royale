using JetBrains.Annotations;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Game setup retrieved when game is started.
/// </summary>
[PublicAPI]
public sealed class GameSetup
{
    /// <summary>
    /// Game type, e.g. "melee".
    /// </summary>
    public string GameType { get; }

    /// <summary>
    /// Width of the arena measured in units.
    /// </summary>
    public int ArenaWidth { get; }

    /// <summary>
    /// Height of the arena measured in units.
    /// </summary>
    public int ArenaHeight { get; }

    /// <summary>
    /// Number of rounds in a battle.
    /// </summary>
    public int NumberOfRounds { get; }

    /// <summary>
    /// Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun is able to fire.
    /// </summary>
    public double GunCoolingRate { get; }

    /// <summary>
    /// Maximum number of inactive turns allowed, where a bot does not take any action before it is
    /// zapped by the game.
    /// </summary>
    public int? MaxInactivityTurns { get; }

    /// <summary>
    /// Timeout duration in microseconds (µs) (where 1 microsecond equals 1/1,000,000 of a second) for sending intent
    /// after receiving a 'tick' message.
    /// </summary>
    public int TurnTimeout { get; }

    /// <summary>
    /// Time limit in microseconds (µs) (where 1 microsecond equals 1/1,000,000 of a second) for sending a 'ready'
    /// message after receiving a 'new battle' message.
    /// </summary>
    public int ReadyTimeout { get; }

    public GameSetup(string gameType, int arenaWidth, int arenaHeight, int numberOfRounds,
        double gunCoolingRate, int? maxInactivityTurns, int turnTimeout, int readyTimeout)
    {
        GameType = gameType;
        ArenaWidth = arenaWidth;
        ArenaHeight = arenaHeight;
        NumberOfRounds = numberOfRounds;
        GunCoolingRate = gunCoolingRate;
        MaxInactivityTurns = maxInactivityTurns;
        TurnTimeout = turnTimeout;
        ReadyTimeout = readyTimeout;
    }
}