import { BotEvent } from "./BotEvent.js";

/** Event occurring when a bot has scanned another bot. */
export class ScannedBotEvent extends BotEvent {
  readonly scannedByBotId: number;
  readonly scannedBotId: number;
  readonly energy: number;
  readonly x: number;
  readonly y: number;
  readonly direction: number;
  readonly speed: number;

  constructor(
    turnNumber: number,
    scannedByBotId: number,
    scannedBotId: number,
    energy: number,
    x: number,
    y: number,
    direction: number,
    speed: number,
  ) {
    super(turnNumber);
    this.scannedByBotId = scannedByBotId;
    this.scannedBotId = scannedBotId;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.speed = speed;
  }
}
