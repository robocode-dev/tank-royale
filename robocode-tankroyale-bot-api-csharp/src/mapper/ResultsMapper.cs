using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  public sealed class ResultsMapper
  {
    public static IEnumerable<BotResults> Map(IEnumerable<Schema.BotResultsForBot> source)
    {
      var botResultsList = new List<BotResults>();
      foreach (var botResults in source)
      {
        botResultsList.Add(Map(botResults));
      }
      return botResultsList;
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