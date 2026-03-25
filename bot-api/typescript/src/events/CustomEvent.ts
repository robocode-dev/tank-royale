import { BotEvent } from "./BotEvent.js";
import { Condition } from "./Condition.js";

/** Event occurring when a custom condition has been met. */
export class CustomEvent extends BotEvent {
  readonly condition: Condition;

  constructor(turnNumber: number, condition: Condition) {
    super(turnNumber);
    this.condition = condition;
  }
}
