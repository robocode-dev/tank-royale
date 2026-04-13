import { BotEvent } from "./BotEvent.js";

/** Event occurring when a turn has been skipped. */
export class SkippedTurnEvent extends BotEvent {
  constructor(turnNumber: number) {
    super(turnNumber);
  }

  override get isCritical(): boolean {
    return true;
  }
}
