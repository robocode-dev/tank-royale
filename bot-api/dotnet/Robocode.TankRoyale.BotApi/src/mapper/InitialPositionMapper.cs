namespace Robocode.TankRoyale.BotApi.Mapper;

public static class InitialPositionMapper
{
    public static Schema.InitialPosition Map(InitialPosition source)
    {
        if (source == null) return null;

        return new Schema.InitialPosition
        {
            X = source.X,
            Y = source.Y,
            Direction = source.Direction
        };
    }
}