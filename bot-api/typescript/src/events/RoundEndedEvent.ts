import { IEvent } from "./IEvent.js";
import { BotResults } from "../BotResults.js";

/** Event occurring when a round has ended. */
export class RoundEndedEvent implements IEvent {
  readonly roundNumber: number;
  readonly turnNumber: number;
  readonly results: BotResults;

  constructor(roundNumber: number, turnNumber: number, results: BotResults) {
    this.roundNumber = roundNumber;
    this.turnNumber = turnNumber;
    this.results = results;
  }
}
