import { BotEvent } from "./BotEvent.js";
import { BulletState } from "../BulletState.js";

/** Event occurring when a bot has been hit by a bullet. */
export class HitByBulletEvent extends BotEvent {
  readonly bullet: BulletState;
  readonly damage: number;
  readonly energy: number;

  constructor(turnNumber: number, bullet: BulletState, damage: number, energy: number) {
    super(turnNumber);
    this.bullet = bullet;
    this.damage = damage;
    this.energy = energy;
  }
}
