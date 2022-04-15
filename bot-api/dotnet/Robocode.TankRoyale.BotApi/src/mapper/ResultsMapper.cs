using System.Collections.Generic;
using System.Linq;

namespace Robocode.TankRoyale.BotApi.Mapper
{
    public sealed class ResultsMapper
    {
        public static IEnumerable<BotResults> Map(IEnumerable<Schema.BotResultsForBot> source)
        {
            return source.Select(Map).ToList();
        }

        private static BotResults Map(Schema.BotResultsForBot source)
        {
            return new BotResults(
                source.Id,
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
}