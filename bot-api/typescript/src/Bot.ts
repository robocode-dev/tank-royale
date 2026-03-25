import { IBot } from "./IBot.js";
import { BaseBot } from "./BaseBot.js";
import { BotInfo } from "./BotInfo.js";
import { Condition } from "./events/Condition.js";
import { BotInternals } from "./internal/BotInternals.js";

/**
 * Abstract bot class that provides convenient methods for movement, turning, and firing.
 * Most bots should extend this class rather than BaseBot.
 */
export abstract class Bot extends BaseBot implements IBot {
  private readonly _botInternals: BotInternals;

  constructor();
  constructor(botInfo: BotInfo);
  constructor(botInfo: BotInfo, serverUrl: string);
  constructor(botInfo: BotInfo, serverUrl: string, serverSecret: string);
  constructor(botInfo?: BotInfo, serverUrl?: string, serverSecret?: string) {
    if (botInfo === undefined) {
      super();
    } else if (serverUrl === undefined) {
      super(botInfo);
    } else if (serverSecret === undefined) {
      super(botInfo, serverUrl);
    } else {
      super(botInfo, serverUrl, serverSecret);
    }
    this._botInternals = new BotInternals(this, this._internals);
  }

  // Override rate/speed setters to go through BotInternals (tracks remaining values)
  override setTurnRate(turnRate: number): void { this._botInternals.setTurnRate(turnRate); }
  override setGunTurnRate(gunTurnRate: number): void { this._botInternals.setGunTurnRate(gunTurnRate); }
  override setRadarTurnRate(radarTurnRate: number): void { this._botInternals.setRadarTurnRate(radarTurnRate); }
  override setTargetSpeed(targetSpeed: number): void { this._botInternals.setTargetSpeed(targetSpeed); }

  isRunning(): boolean { return this._internals.isRunning(); }

  run(): void { /* override in subclass */ }

  // Setter movement (non-blocking)
  setForward(distance: number): void { this._botInternals.setForward(distance); }
  setBack(distance: number): void { this._botInternals.setForward(-distance); }
  setTurnLeft(degrees: number): void { this._botInternals.setTurnLeft(degrees); }
  setTurnRight(degrees: number): void { this._botInternals.setTurnLeft(-degrees); }
  setTurnGunLeft(degrees: number): void { this._botInternals.setTurnGunLeft(degrees); }
  setTurnGunRight(degrees: number): void { this._botInternals.setTurnGunLeft(-degrees); }
  setTurnRadarLeft(degrees: number): void { this._botInternals.setTurnRadarLeft(degrees); }
  setTurnRadarRight(degrees: number): void { this._botInternals.setTurnRadarLeft(-degrees); }

  // Blocking movement
  forward(distance: number): void { this._botInternals.forward(distance); }
  back(distance: number): void { this._botInternals.forward(-distance); }
  turnLeft(degrees: number): void { this._botInternals.turnLeft(degrees); }
  turnRight(degrees: number): void { this._botInternals.turnLeft(-degrees); }
  turnGunLeft(degrees: number): void { this._botInternals.turnGunLeft(degrees); }
  turnGunRight(degrees: number): void { this._botInternals.turnGunLeft(-degrees); }
  turnRadarLeft(degrees: number): void { this._botInternals.turnRadarLeft(degrees); }
  turnRadarRight(degrees: number): void { this._botInternals.turnRadarLeft(-degrees); }

  // Remaining getters
  getDistanceRemaining(): number { return this._botInternals.distanceRemaining; }
  getTurnRemaining(): number { return this._botInternals.turnRemaining; }
  getGunTurnRemaining(): number { return this._botInternals.gunTurnRemaining; }
  getRadarTurnRemaining(): number { return this._botInternals.radarTurnRemaining; }

  // Blocking actions
  fire(firepower: number): void { this._botInternals.fire(firepower); }

  override setStop(): void;
  override setStop(overwrite: boolean): void;
  override setStop(overwrite?: boolean): void { this._internals.setStop(overwrite); }

  stop(): void;
  stop(overwrite: boolean): void;
  stop(overwrite?: boolean): void { this._botInternals.stop(overwrite); }

  resume(): void { this._botInternals.resume(); }
  rescan(): void { this._botInternals.rescan(); }
  waitFor(condition: Condition): void { this._botInternals.waitFor(() => condition.test()); }
}
