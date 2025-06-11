using static Robocode.TankRoyale.BotApi.Util.ColorUtil;

namespace Robocode.TankRoyale.BotApi.Mapper;

static class BotStateMapper
{
    internal static BotState Map(Schema.BotState source)
    {
        return new BotState(
            source.IsDroid == false,
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
            source.EnemyCount,
            FromWebColor(source.BodyColor),
            FromWebColor(source.TurretColor),
            FromWebColor(source.RadarColor),
            FromWebColor(source.BulletColor),
            FromWebColor(source.ScanColor),
            FromWebColor(source.TracksColor),
            FromWebColor(source.GunColor),
            source.IsDebuggingEnabled == true
        );
    }
}