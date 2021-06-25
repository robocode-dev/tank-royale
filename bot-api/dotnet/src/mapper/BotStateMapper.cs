namespace Robocode.TankRoyale.BotApi.Mapper
{
  public sealed class BotStateMapper
  {
    public static BotState Map(Schema.BotState source)
    {
      return new BotState(
        source.Energy,
        source.X,
        source.Y,
        source.Direction,
        source.GunDirection,
        source.RadarDirection,
        source.RadarSweep,
        source.Speed,
        source.TurnRate,
        source.GunTurnRate,
        source.RadarTurnRate,
        source.GunHeat,
        source.BodyColor,
        source.TurretColor,
        source.RadarColor,
        source.BulletColor,
        source.ScanColor,
        source.TracksColor,
        source.GunColor
      );
    }
  }
}