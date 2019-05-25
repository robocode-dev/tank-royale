import { BotState, BulletState } from "./States";
import { BotResultsForObservers } from "./BotResults";

export enum EventType {
  BotDeathEvent = "BotDeathEvent",
  BotListUpdate = "BotListUpdate",
  BulletHitBotEvent = "BulletHitBotEvent",
  GameStartedEventForObserver = "GameStartedEventForObserver",
  GameAbortedEventForObserver = "GameAbortedEventForObserver",
  GameEndedEventForObserver = "GameEndedEventForObserver",
  GamePausedEventForObserver = "GamePausedEventForObserver",
  GameResumedEventForObserver = "GameResumedEventForObserver",
  ScannedBotEvent = "ScannedBotEvent",
  TickEventForObserver = "TickEventForObserver",
}

export class Event {
  public type: EventType;

  constructor(type: EventType) {
    this.type = type;
  }
}

export class GameStartedEventForObserver extends Event {
  constructor() {
    super(EventType.GameStartedEventForObserver);
  }
}

export class GameAbortedEventForObserver extends Event {
  constructor() {
    super(EventType.GameAbortedEventForObserver);
  }
}

export class GameEndedEventForObserver extends Event {
  public numberOfRounds: number = 0;
  public results: BotResultsForObservers[] = [];

  constructor() {
    super(EventType.GameEndedEventForObserver);
  }
}

export class GamePausedEventForObserver extends Event {
  constructor() {
    super(EventType.GamePausedEventForObserver);
  }
}

export class GameResumedEventForObserver extends Event {
  constructor() {
    super(EventType.GameResumedEventForObserver);
  }
}

export class TickEventForObserver extends Event {
  public botStates: BotState[] = [];
  public bulletStates: BulletState[] = [];
  public events: Event[] = [];

  public explosions: any[] = [];

  constructor() {
    super(EventType.TickEventForObserver);
  }
}

export class BotDeathEvent extends Event {
  public victimId: number;

  constructor(victimId: number) {
    super(EventType.BotDeathEvent);
    this.victimId = victimId;
  }
}

export class BulletHitBotEvent extends Event {
  public bullet: BulletState;
  public victimId: number;
  public damage: number;
  public energy: number;

  constructor(bullet: BulletState, victimId: number, damage: number, energy: number) {
    super(EventType.BotDeathEvent);
    this.bullet = bullet;
    this.victimId = victimId;
    this.damage = damage;
    this.energy = energy;
  }
}

export class ScannedBotEvent extends Event {
  public x: number = 0;
  public y: number = 0;
}
