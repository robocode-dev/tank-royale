import { IEvent } from "./IEvent.js";
import { BotResults } from "../BotResults.js";

/** Event occurring when a game has ended. */
export class GameEndedEvent implements IEvent {
  readonly numberOfRounds: number;
  readonly results: BotResults;

  constructor(numberOfRounds: number, results: BotResults) {
    this.numberOfRounds = numberOfRounds;
    this.results = results;
  }
}
