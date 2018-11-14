class BotResultsForBots {
  public id: number = 0;
  public rank: number = 0;
  public survival: number = 0;
  public lastSurvivorBonus: number = 0;
  public bulletDamage: number = 0;
  public bulletKillBonus: number = 0;
  public ramDamage: number = 0;
  public ramKillBonus: number = 0;
  public totalScore: number = 0;
  public firstPlaces: number = 0;
  public secondPlaces: number = 0;
  public thirdPlaces: number = 0;
}

export class BotResultsForObservers extends BotResultsForBots {
  public name: string = "";
  public version: string = "";
}
