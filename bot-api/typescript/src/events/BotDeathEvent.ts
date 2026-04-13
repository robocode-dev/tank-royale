import { BotEvent } from "./BotEvent.js";

/** Event occurring when another bot has died. */
export class BotDeathEvent extends BotEvent {
  readonly victimId: number;

  constructor(turnNumber: number, victimId: number) {
    super(turnNumber);
    this.victimId = victimId;
  }
}
