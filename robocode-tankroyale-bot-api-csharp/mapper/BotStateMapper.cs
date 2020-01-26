namespace Robocode.TankRoyale.BotApi
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
        source.GunHeat
      );
    }
  }
}