namespace Robocode.TankRoyale
{
  public interface IBaseBot
  {
    /// <summary>
    /// Main method for start running the bot.
    /// </summary>
    void Start();

    /// <summary>
    /// Commits the current actions for the current turn. This method must be called in order to send
    /// the bot actions to the server, and MUST before the turn timeout occurs. The turn timeout is
    /// started when the GameStartedEvent and TickEvent occurs. If Go() is called too late,
    /// SkippedTurnEvents will occur. Actions are set by calling the setter methods prior to calling
    /// the Go() method: SetTurnRate(), SetGunTurnRate(), SetRadarTurnRate(), SetTargetSpeed(), and
    /// SetFire().
    /// </summary>
    void Go();

    /// <summary>
    /// Property containing the unique id of this bot in the battle. Available when game has started.
    /// </summary>
    int MyId { get; }

    /// <summary>
    /// Property containing the game variant, e.g. "Tank Royale" for Robocode Tank Royale.
    /// </summary>
    string Variant { get; }

    /// <summary>
    /// Property containing the game version, e.g. "1.0.0"
    /// </summary>
    string Version { get; }

    /// <summary>
    /// Property containing the game type, e.g. "melee".
    ///
    /// Available when game has started.
    /// </summary>
    string GameType { get; }

    /// <summary>
    /// Property containing the width of the arena measured in pixels.
    ///
    /// Available when game has started.
    /// </summary>
    int ArenaWidth { get; }

    /// <summary>
    /// Property containing the height of the arena measured in pixels.
    ///
    /// Available when game has started.
    /// </summary>
    int ArenaHeight { get; }

    /// <summary>
    /// Property containing the number of rounds in a battle.
    ///
    /// Available when game has started.
    /// </summary>
    int NumberOfRounds { get; }

    /// <summary>
    /// Property containing the gun cooling rate. The gun needs to cool down to a gun heat of zero
    /// before the gun is able to fire. The gun cooling rate determines how fast the gun cools down.
    /// That is, the gun cooling rate is subtracted from the gun heat each turn until the gun heat
    /// reaches zero.
    ///
    /// Available when game has started.
    /// </summary>
    double GunCoolingRate { get; }

    /// <summary>
    /// Property containing the maximum number of inactive turns allowed, where a bot does not take any
    /// action before it is zapped by the game.
    ///
    /// Available when game has started.
    /// </summary>
    int MaxInactivityTurns { get; }

    /// <summary>
    /// Property containing turn timeout in microseconds (1 / 1,000,000 second). The turn timeout is
    /// important as the bot need to take action by calling Go() before the turn timeout occurs. As
    /// soon as the TickEvent is triggered, i.e. when OnTick() is called, you need to call Go() to take
    /// action before the turn timeout occurs. Otherwise your bot will receive SkippedTurnEvent(s).
    ///
    /// Available when game has started.
    /// </summary>
    /// <seealso cref="TimeLeft"/>
    /// <seealso cref="Go"/>
    int TurnTimeout { get; }

    /// <summary>
    /// Property containing the number of microseconds left for this round before the bot will skip the
    /// turn. Make sure to call Go() before the time runs out.
    /// </summary>
    /// <seealso cref="TurnTimeout"/>
    /// <seealso cref="Go"/>
    int TimeLeft { get; }

    /// <summary>
    /// Property containing the current round number.
    /// </summary>
    /// <value></value>
    int RoundNumber { get; }

    /// <summary>
    /// Property containing the current turn number.
    /// </summary>
    /// <value></value>
    int TurnNumber { get; }
  }
}