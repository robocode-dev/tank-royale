using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Event occurring when game has just started.
/// </summary>
public sealed class GameStartedEvent : IEvent
{
    /// <summary>The id used for identifying your bot in the current battle.</summary>
    public int MyId { get; }

    /// <summary>The initial position of the bot.</summary>
    public InitialPosition InitialPosition { get; }
    
    /// <summary>The game setup for the battle just started.</summary>
    public GameSetup GameSetup { get; }

    /// <summary>
    /// Initializes a new instance of the GameStartedEvent class.
    /// </summary>
    /// <param name="myId">ID used for identifying your bot in the current battle.</param>
    /// <param name="initialPosition">Initial position of the bot.</param>
    /// <param name="gameSetup">Game setup for the battle just started.</param>
    [JsonConstructor]
    public GameStartedEvent(int myId, InitialPosition initialPosition, GameSetup gameSetup) =>
        (MyId, InitialPosition, GameSetup) = (myId, initialPosition, gameSetup);
}