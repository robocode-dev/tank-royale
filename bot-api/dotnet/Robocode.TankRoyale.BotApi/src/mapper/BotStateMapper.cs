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
                Color.FromRgb(source.BodyColor),
                Color.FromRgb(source.TurretColor),
                Color.FromRgb(source.RadarColor),
                Color.FromRgb(source.BulletColor),
                Color.FromRgb(source.ScanColor),
                Color.FromRgb(source.TracksColor),
                Color.FromRgb(source.GunColor)
            );
        }
    }
}