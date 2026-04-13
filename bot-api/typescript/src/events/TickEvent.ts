import { BotEvent } from "./BotEvent.js";
import { BotState } from "../BotState.js";
import { BulletState } from "../BulletState.js";

/** Event occurring whenever a new turn in a round has started. */
export class TickEvent extends BotEvent {
  readonly roundNumber: number;
  readonly botState: BotState;
  readonly bulletStates: BulletState[];
  readonly events: BotEvent[];

  constructor(
    turnNumber: number,
    roundNumber: number,
    botState: BotState,
    bulletStates: BulletState[],
    events: BotEvent[],
  ) {
    super(turnNumber);
    this.roundNumber = roundNumber;
    this.botState = botState;
    this.bulletStates = bulletStates;
    this.events = events;
  }
}
