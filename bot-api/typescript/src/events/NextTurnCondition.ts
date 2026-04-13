import { Condition } from "./Condition.js";

/**
 * Condition that is met when the turn number has advanced past the turn
 * at which this condition was created.
 */
export class NextTurnCondition extends Condition {
  private readonly creationTurnNumber: number;
  private readonly getTurnNumber: () => number;

  constructor(getTurnNumber: () => number) {
    super();
    this.getTurnNumber = getTurnNumber;
    this.creationTurnNumber = getTurnNumber();
  }

  override test(): boolean {
    return this.getTurnNumber() > this.creationTurnNumber;
  }
}
