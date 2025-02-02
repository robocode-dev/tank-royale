using static Robocode.TankRoyale.BotApi.Util.ColorUtil;

namespace Robocode.TankRoyale.BotApi.Mapper;

internal static class BotStateMapper
{
    public static BotState Map(Schema.Game.BotState source)
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
            FromString(source.BodyColor),
            FromString(source.TurretColor),
            FromString(source.RadarColor),
            FromString(source.BulletColor),
            FromString(source.ScanColor),
            FromString(source.TracksColor),
            FromString(source.GunColor),
            source.IsDebuggingEnabled == true
        );
    }
}