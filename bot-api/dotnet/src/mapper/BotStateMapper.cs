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
        Color.FromRgbInt(source.BodyColor),
        Color.FromRgbInt(source.TurretColor),
        Color.FromRgbInt(source.RadarColor),
        Color.FromRgbInt(source.BulletColor),
        Color.FromRgbInt(source.ScanColor),
        Color.FromRgbInt(source.TracksColor),
        Color.FromRgbInt(source.GunColor)
      );
    }
  }
}