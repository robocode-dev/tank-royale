import { BotEvent } from "./BotEvent.js";

/** Event occurring when your bot has died. */
export class DeathEvent extends BotEvent {
  constructor(turnNumber: number) {
    super(turnNumber);
  }

  override get isCritical(): boolean {
    return true;
  }
}
