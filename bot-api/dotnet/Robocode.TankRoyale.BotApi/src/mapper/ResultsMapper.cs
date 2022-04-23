using System.Collections.Generic;
using System.Linq;

namespace Robocode.TankRoyale.BotApi.Mapper
{
    public sealed class ResultsMapper
    {
        public static BotResults Map(Schema.BotResultsForBot source)
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
}