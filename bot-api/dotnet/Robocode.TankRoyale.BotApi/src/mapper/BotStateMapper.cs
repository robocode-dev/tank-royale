using static Robocode.TankRoyale.BotApi.Color;

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
        FromString(source.BodyColor),
        FromString(source.TurretColor),
        FromString(source.RadarColor),
        FromString(source.BulletColor),
        FromString(source.ScanColor),
        FromString(source.TracksColor),
        FromString(source.GunColor)
      );
    }
  }
}