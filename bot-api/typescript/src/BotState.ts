/**
 * Represents the state of a bot at a specific turn.
 */
export class BotState {
  readonly isDroid: boolean;
  readonly energy: number;
  readonly x: number;
  readonly y: number;
  readonly direction: number;
  readonly gunDirection: number;
  readonly radarDirection: number;
  readonly radarSweep: number;
  readonly speed: number;
  readonly turnRate: number;
  readonly gunTurnRate: number;
  readonly radarTurnRate: number;
  readonly gunHeat: number;
  readonly enemyCount: number;
  readonly bodyColor: string | null;
  readonly turretColor: string | null;
  readonly radarColor: string | null;
  readonly bulletColor: string | null;
  readonly scanColor: string | null;
  readonly tracksColor: string | null;
  readonly gunColor: string | null;
  readonly isDebuggingEnabled: boolean;

  constructor(
    isDroid: boolean,
    energy: number,
    x: number,
    y: number,
    direction: number,
    gunDirection: number,
    radarDirection: number,
    radarSweep: number,
    speed: number,
    turnRate: number,
    gunTurnRate: number,
    radarTurnRate: number,
    gunHeat: number,
    enemyCount: number,
    bodyColor: string | null,
    turretColor: string | null,
    radarColor: string | null,
    bulletColor: string | null,
    scanColor: string | null,
    tracksColor: string | null,
    gunColor: string | null,
    isDebuggingEnabled: boolean,
  ) {
    this.isDroid = isDroid;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.gunDirection = gunDirection;
    this.radarDirection = radarDirection;
    this.radarSweep = radarSweep;
    this.speed = speed;
    this.turnRate = turnRate;
    this.gunTurnRate = gunTurnRate;
    this.radarTurnRate = radarTurnRate;
    this.gunHeat = gunHeat;
    this.enemyCount = enemyCount;
    this.bodyColor = bodyColor;
    this.turretColor = turretColor;
    this.radarColor = radarColor;
    this.bulletColor = bulletColor;
    this.scanColor = scanColor;
    this.tracksColor = tracksColor;
    this.gunColor = gunColor;
    this.isDebuggingEnabled = isDebuggingEnabled;
  }
}
