/**
 * Represents the results of a bot at the end of a battle.
 */
export class BotResults {
  readonly rank: number;
  readonly survival: number;
  readonly lastSurvivorBonus: number;
  readonly bulletDamage: number;
  readonly bulletKillBonus: number;
  readonly ramDamage: number;
  readonly ramKillBonus: number;
  readonly totalScore: number;
  readonly firstPlaces: number;
  readonly secondPlaces: number;
  readonly thirdPlaces: number;

  constructor(
    rank: number,
    survival: number,
    lastSurvivorBonus: number,
    bulletDamage: number,
    bulletKillBonus: number,
    ramDamage: number,
    ramKillBonus: number,
    totalScore: number,
    firstPlaces: number,
    secondPlaces: number,
    thirdPlaces: number,
  ) {
    this.rank = rank;
    this.survival = survival;
    this.lastSurvivorBonus = lastSurvivorBonus;
    this.bulletDamage = bulletDamage;
    this.bulletKillBonus = bulletKillBonus;
    this.ramDamage = ramDamage;
    this.ramKillBonus = ramKillBonus;
    this.totalScore = totalScore;
    this.firstPlaces = firstPlaces;
    this.secondPlaces = secondPlaces;
    this.thirdPlaces = thirdPlaces;
  }
}
