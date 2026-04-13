import { BotEvent } from "./BotEvent.js";
import { BulletState } from "../BulletState.js";

/** Event occurring when a bullet has hit another bullet. */
export class BulletHitBulletEvent extends BotEvent {
  readonly bullet: BulletState;
  readonly hitBullet: BulletState;

  constructor(turnNumber: number, bullet: BulletState, hitBullet: BulletState) {
    super(turnNumber);
    this.bullet = bullet;
    this.hitBullet = hitBullet;
  }
}
