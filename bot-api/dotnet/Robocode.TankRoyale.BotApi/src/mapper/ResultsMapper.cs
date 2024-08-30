namespace Robocode.TankRoyale.BotApi.Mapper;

public static class ResultsMapper
{
    public static BotResults Map(Schema.Game.ResultsForBot source)
    {
        return new BotResults(
            source.Rank,
            source.Survival,
            source.LastSurvivorBonus,
            source.BulletDamage,
            source.BulletKillBonus,
            source.RamDamage,
            source.RamKillBonus,
            source.TotalScore,
            source.FirstPlaces,
            source.SecondPlaces,
            source.ThirdPlaces
        );
    }
}