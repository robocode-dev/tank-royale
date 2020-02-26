namespace Robocode.TankRoyale.BotApi
{
  public sealed class GameSetupMapper
  {
    public static GameSetup Map(Schema.GameSetup source)
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
}