import { BotEvent } from "./BotEvent.js";

/** Event occurring when a bot has won a round. */
export class WonRoundEvent extends BotEvent {
  constructor(turnNumber: number) {
    super(turnNumber);
  }
}
