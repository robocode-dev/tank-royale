namespace Robocode.TankRoyale.BotApi.Mapper;

public sealed class InitialPositionMapper
{
    public static Schema.InitialPosition Map(InitialPosition source)
    {
        if (source == null) return null;

        return new Schema.InitialPosition
        {
            X = source.X,
            Y = source.Y,
            Angle = source.Angle
        };
    }
}