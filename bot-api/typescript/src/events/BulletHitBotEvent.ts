import { BotEvent } from "./BotEvent.js";
import { BulletState } from "../BulletState.js";

/** Event occurring when a bullet has hit another bot. */
export class BulletHitBotEvent extends BotEvent {
  readonly victimId: number;
  readonly bullet: BulletState;
  readonly damage: number;
  readonly energy: number;

  constructor(turnNumber: number, victimId: number, bullet: BulletState, damage: number, energy: number) {
    super(turnNumber);
    this.victimId = victimId;
    this.bullet = bullet;
    this.damage = damage;
    this.energy = energy;
  }
}
