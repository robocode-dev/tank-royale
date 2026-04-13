import { BotEvent } from "./BotEvent.js";

/** Event occurring when a bot has collided with another bot. */
export class HitBotEvent extends BotEvent {
  readonly victimId: number;
  readonly energy: number;
  readonly x: number;
  readonly y: number;
  readonly isRammed: boolean;

  constructor(turnNumber: number, victimId: number, energy: number, x: number, y: number, isRammed: boolean) {
    super(turnNumber);
    this.victimId = victimId;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.isRammed = isRammed;
  }
}
