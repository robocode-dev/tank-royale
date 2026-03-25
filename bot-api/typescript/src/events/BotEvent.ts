/** Base class for all bot events occurring during a battle. */
export abstract class BotEvent {
  readonly turnNumber: number;

  protected constructor(turnNumber: number) {
    this.turnNumber = turnNumber;
  }

  get isCritical(): boolean {
    return false;
  }
}
