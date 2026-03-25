import { IEvent } from "./IEvent.js";

/** Event occurring when a new round has started. */
export class RoundStartedEvent implements IEvent {
  readonly roundNumber: number;

  constructor(roundNumber: number) {
    this.roundNumber = roundNumber;
  }
}
