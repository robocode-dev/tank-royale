import { IEvent } from "./IEvent.js";

/** Base class for all bot events occurring during a battle. */
export abstract class BotEvent implements IEvent {
  readonly turnNumber: number;

  protected constructor(turnNumber: number) {
    this.turnNumber = turnNumber;
  }

  get isCritical(): boolean {
    return false;
  }
}
