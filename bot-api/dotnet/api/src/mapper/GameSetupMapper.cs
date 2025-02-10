namespace Robocode.TankRoyale.BotApi.Mapper;

public static class GameSetupMapper
{
    public static GameSetup Map(Schema.Game.GameSetup source)
    {
        return new GameSetup(
            source.GameType,
            source.ArenaWidth,
            source.ArenaHeight,
            source.NumberOfRounds,
            source.GunCoolingRate,
            source.MaxInactivityTurns,
            source.TurnTimeout,
            source.ReadyTimeout
        );
    }
}