import { BotState, BulletState, Explosion } from "./States";
import { Point } from "./Types";

export enum EventType {
  BotDeathEvent = "botDeathEvent",
  BulletHitBotEvent = "bulletHitBotEvent",
  GameStartedEventForObserver = "gameStartedEventForObserver",
  GameAbortedEventForObserver = "gameAbortedEventForObserver",
  GameEndedEventForObserver = "gameEndedEventForObserver",
  GamePausedEventForObserver = "gamePausedEventForObserver",
  GameResumedEventForObserver = "gameResumedEventForObserver",
  ScannedBotEvent = "scannedBotEvent",
  TickEventForObserver = "tickEventForObserver",
}

export class Event {
  public type?: EventType;
}

export class GameStartedEvent extends Event {}

export class GameAbortedEvent extends Event {}

export class GameEndedEvent extends Event {}

export class GamePausedEvent extends Event {}

export class GameResumedEvent extends Event {}

export class TickEvent extends Event {
  public botStates?: BotState[];
  public bulletStates?: BulletState[];
  public events?: Event[];

  public explosions?: Explosion[];
}

export class BotDeathEvent extends Event {
  public victimId: number;

  constructor(victimId: number) {
    super();
    this.victimId = victimId;
  }
}

export class BulletHitBotEvent extends Event {
  public bullet?: BulletState;
  public victimId?: number;
  public damage?: number;
  public energy?: number;
}

export class ScannedBotEvent extends Event {
  public position?: Point;
}
