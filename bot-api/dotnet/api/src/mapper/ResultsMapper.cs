namespace Robocode.TankRoyale.BotApi.Mapper;

static class ResultsMapper
{
    internal static BotResults Map(Schema.ResultsForBot source)
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