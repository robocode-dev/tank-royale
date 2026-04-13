import { BotEvent } from "./BotEvent.js";

/** Event occurring when a bot has hit a wall. */
export class HitWallEvent extends BotEvent {
  constructor(turnNumber: number) {
    super(turnNumber);
  }
}
