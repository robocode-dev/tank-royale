import { ResultsForBot } from "../protocol/schema.js";
import { BotResults } from "../BotResults.js";

/** Maps schema ResultsForBot to API BotResults. */
export class ResultsMapper {
  static map(s: ResultsForBot): BotResults {
    return new BotResults(
      s.rank,
      s.survival,
      s.lastSurvivorBonus,
      s.bulletDamage,
      s.bulletKillBonus,
      s.ramDamage,
      s.ramKillBonus,
      s.totalScore,
      s.firstPlaces,
      s.secondPlaces,
      s.thirdPlaces,
    );
  }
}
