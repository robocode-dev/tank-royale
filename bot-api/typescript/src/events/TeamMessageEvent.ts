import { BotEvent } from "./BotEvent.js";

/** Event occurring when a team message has been received from a teammate. */
export class TeamMessageEvent extends BotEvent {
  readonly message: unknown;
  readonly senderId: number;

  constructor(turnNumber: number, message: unknown, senderId: number) {
    super(turnNumber);
    if (message === null || message === undefined) {
      throw new Error("message cannot be null");
    }
    this.message = message;
    this.senderId = senderId;
  }
}
