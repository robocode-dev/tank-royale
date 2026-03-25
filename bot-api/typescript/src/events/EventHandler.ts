/** A subscriber entry with optional priority (higher = called first). */
interface Subscriber<T> {
  handler: (event: T) => void;
  priority: number;
}

/** Generic event handler supporting subscribe/unsubscribe/publish with priority ordering. */
export class EventHandler<T> {
  private subscribers: Subscriber<T>[] = [];

  subscribe(handler: (event: T) => void, priority = 0): void {
    this.subscribers.push({ handler, priority });
    this.subscribers.sort((a, b) => b.priority - a.priority);
  }

  unsubscribe(handler: (event: T) => void): void {
    this.subscribers = this.subscribers.filter((s) => s.handler !== handler);
  }

  publish(event: T): void {
    for (const sub of [...this.subscribers]) {
      sub.handler(event);
    }
  }
}
