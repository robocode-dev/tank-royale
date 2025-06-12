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
            FromHexColor(source.BodyColor),
            FromHexColor(source.TurretColor),
            FromHexColor(source.RadarColor),
            FromHexColor(source.BulletColor),
            FromHexColor(source.ScanColor),
            FromHexColor(source.TracksColor),
            FromHexColor(source.GunColor),
            source.IsDebuggingEnabled == true
        );
    }
}