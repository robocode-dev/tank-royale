/**
 * Game setup retrieved when game is started.
 */
export class GameSetup {
  readonly gameType: string;
  readonly arenaWidth: number;
  readonly arenaHeight: number;
  readonly numberOfRounds: number;
  readonly gunCoolingRate: number;
  readonly maxInactivityTurns: number;
  readonly turnTimeout: number;
  readonly readyTimeout: number;

  constructor(
    gameType: string,
    arenaWidth: number,
    arenaHeight: number,
    numberOfRounds: number,
    gunCoolingRate: number,
    maxInactivityTurns: number,
    turnTimeout: number,
    readyTimeout: number,
  ) {
    this.gameType = gameType;
    this.arenaWidth = arenaWidth;
    this.arenaHeight = arenaHeight;
    this.numberOfRounds = numberOfRounds;
    this.gunCoolingRate = gunCoolingRate;
    this.maxInactivityTurns = maxInactivityTurns;
    this.turnTimeout = turnTimeout;
    this.readyTimeout = readyTimeout;
  }
}
