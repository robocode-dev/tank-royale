namespace Robocode.TankRoyale.BotApi.Mapper;

static class GameSetupMapper
{
    internal static GameSetup Map(Schema.GameSetup source)
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