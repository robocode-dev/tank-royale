import { BotEvent } from "./BotEvent.js";
import { TickEvent } from "./TickEvent.js";
import { CustomEvent } from "./CustomEvent.js";
import { Condition } from "./Condition.js";
import { EventPriorities } from "./EventPriorities.js";
import { EventInterruption } from "./EventInterruption.js";
import { BotEventHandlers } from "./BotEventHandlers.js";

const MAX_QUEUE_SIZE = 256;
const MAX_EVENT_AGE = 2;

/** Manages the queue of bot events, including sorting, dispatching, and interruption. */
export class EventQueue {
  private events: BotEvent[] = [];
  private conditions: Condition[] = [];
  private readonly priorities: EventPriorities;
  private readonly interruption: EventInterruption;
  private currentEventPriority = -1;
  private currentEventInterruptible = false;

  constructor(priorities: EventPriorities, interruption: EventInterruption) {
    this.priorities = priorities;
    this.interruption = interruption;
  }

  addEvent(event: BotEvent): void {
    if (this.events.length >= MAX_QUEUE_SIZE) {
      return;
    }
    this.events.push(event);
  }

  addEventsFromTick(tickEvent: TickEvent): void {
    this.addEvent(tickEvent);
    for (const e of tickEvent.events) {
      this.addEvent(e);
    }
  }

  removeOldEvents(turnNumber: number): void {
    this.events = this.events.filter(
      (e) => e.isCritical || turnNumber - e.turnNumber <= MAX_EVENT_AGE,
    );
  }

  sortEvents(): void {
    this.events.sort((a, b) => {
      // Critical events first
      if (a.isCritical !== b.isCritical) return a.isCritical ? -1 : 1;
      // Older turn first (lower turnNumber first)
      if (a.turnNumber !== b.turnNumber) return a.turnNumber - b.turnNumber;
      // Higher priority first
      const pa = this.priorities.getPriority(a.constructor.name);
      const pb = this.priorities.getPriority(b.constructor.name);
      return pb - pa;
    });
  }

  dispatchEvents(turnNumber: number, handlers: BotEventHandlers): void {
    this.removeOldEvents(turnNumber);
    this.addCustomEvents(turnNumber);
    this.sortEvents();

    const dispatched = [...this.events];
    this.events = [];

    for (const event of dispatched) {
      const priority = this.priorities.getPriority(event.constructor.name);

      // If a higher-priority event arrives while dispatching an interruptible handler, stop
      if (
        this.currentEventPriority >= 0 &&
        priority > this.currentEventPriority &&
        this.currentEventInterruptible
      ) {
        break;
      }

      const prevPriority = this.currentEventPriority;
      const prevInterruptible = this.currentEventInterruptible;

      this.currentEventPriority = priority;
      this.currentEventInterruptible = this.interruption.isInterruptible(event.constructor.name);

      handlers.fireEvent(event);

      this.currentEventPriority = prevPriority;
      this.currentEventInterruptible = prevInterruptible;
    }
  }

  addCustomEvents(turnNumber: number): void {
    for (const condition of this.conditions) {
      if (condition.test()) {
        this.addEvent(new CustomEvent(turnNumber, condition));
      }
    }
  }

  addCondition(condition: Condition): void {
    this.conditions.push(condition);
  }

  removeCondition(condition: Condition): void {
    this.conditions = this.conditions.filter((c) => c !== condition);
  }

  clear(): void {
    this.events = [];
    this.conditions = [];
    this.currentEventPriority = -1;
    this.currentEventInterruptible = false;
  }

  setCurrentEventInterruptible(interruptible: boolean): void {
    this.currentEventInterruptible = interruptible;
  }

  getEvents(): readonly BotEvent[] {
    return this.events;
  }
}
