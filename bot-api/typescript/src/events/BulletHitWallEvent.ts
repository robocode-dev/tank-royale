import { BotEvent } from "./BotEvent.js";
import { BulletState } from "../BulletState.js";

/** Event occurring when a bullet has hit a wall. */
export class BulletHitWallEvent extends BotEvent {
  readonly bullet: BulletState;

  constructor(turnNumber: number, bullet: BulletState) {
    super(turnNumber);
    this.bullet = bullet;
  }
}
