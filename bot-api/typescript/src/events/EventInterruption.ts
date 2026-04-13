/** Tracks which event types have the interruptible flag set. */
export class EventInterruption {
  private readonly interruptible: Map<string, boolean> = new Map();

  isInterruptible(eventType: string): boolean {
    return this.interruptible.get(eventType) === true;
  }

  setInterruptible(eventType: string, interruptible: boolean): void {
    this.interruptible.set(eventType, interruptible);
  }
}
