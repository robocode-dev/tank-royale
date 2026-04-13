/**
 * Protocol schema DTOs — plain interfaces matching the JSON wire format exactly (camelCase).
 * These are separate from the public API types and represent raw server messages.
 */

import { MessageType } from "./MessageType.js";

// ---------------------------------------------------------------------------
// Base message
// ---------------------------------------------------------------------------

export interface Message {
  type: MessageType;
}

// ---------------------------------------------------------------------------
// Schema value types (1.13–1.18)
// ---------------------------------------------------------------------------

/** Schema BotState — raw wire format from server (1.13) */
export interface BotState {
  isDroid: boolean;
  energy: number;
  x: number;
  y: number;
  direction: number;
  gunDirection: number;
  radarDirection: number;
  radarSweep: number;
  speed: number;
  turnRate: number;
  gunTurnRate: number;
  radarTurnRate: number;
  gunHeat: number;
  enemyCount: number;
  bodyColor?: string | null;
  turretColor?: string | null;
  radarColor?: string | null;
  bulletColor?: string | null;
  scanColor?: string | null;
  tracksColor?: string | null;
  gunColor?: string | null;
  isDebuggingEnabled: boolean;
}

/** Schema BulletState — raw wire format from server (1.14) */
export interface BulletState {
  bulletId: number;
  ownerId: number;
  power: number;
  x: number;
  y: number;
  direction: number;
  color?: string | null;
}

/** Schema GameSetup — raw wire format from server (1.15) */
export interface GameSetup {
  gameType: string;
  arenaWidth: number;
  arenaHeight: number;
  numberOfRounds: number;
  gunCoolingRate: number;
  maxInactivityTurns: number;
  turnTimeout: number;
  readyTimeout: number;
}

/** Schema ResultsForBot — raw wire format from server (1.16) */
export interface ResultsForBot {
  rank: number;
  survival: number;
  lastSurvivorBonus: number;
  bulletDamage: number;
  bulletKillBonus: number;
  ramDamage: number;
  ramKillBonus: number;
  totalScore: number;
  firstPlaces: number;
  secondPlaces: number;
  thirdPlaces: number;
}

/** Schema InitialPosition — raw wire format (1.17) */
export interface InitialPosition {
  x?: number | null;
  y?: number | null;
  direction?: number | null;
}

/** Schema TeamMessage — raw wire format (1.18) */
export interface TeamMessage {
  message: string;
  messageType: string;
  receiverId?: number | null;
}

// ---------------------------------------------------------------------------
// In-tick event interfaces (1.11, 1.12)
// ---------------------------------------------------------------------------

export interface BotDeathEvent extends Message {
  type: MessageType.BotDeathEvent;
  turnNumber: number;
  victimId: number;
}

export interface BotHitBotEvent extends Message {
  type: MessageType.BotHitBotEvent;
  turnNumber: number;
  victimId: number;
  energy: number;
  x: number;
  y: number;
  rammed: boolean;
}

export interface BotHitWallEvent extends Message {
  type: MessageType.BotHitWallEvent;
  turnNumber: number;
}

export interface BulletFiredEvent extends Message {
  type: MessageType.BulletFiredEvent;
  turnNumber: number;
  bullet: BulletState;
}

export interface BulletHitBotEvent extends Message {
  type: MessageType.BulletHitBotEvent;
  turnNumber: number;
  victimId: number;
  bullet: BulletState;
  damage: number;
  energy: number;
}

export interface BulletHitBulletEvent extends Message {
  type: MessageType.BulletHitBulletEvent;
  turnNumber: number;
  bullet: BulletState;
  hitBullet: BulletState;
}

export interface BulletHitWallEvent extends Message {
  type: MessageType.BulletHitWallEvent;
  turnNumber: number;
  bullet: BulletState;
}

export interface ScannedBotEvent extends Message {
  type: MessageType.ScannedBotEvent;
  turnNumber: number;
  scannedByBotId: number;
  scannedBotId: number;
  energy: number;
  x: number;
  y: number;
  direction: number;
  speed: number;
}

export interface WonRoundEvent extends Message {
  type: MessageType.WonRoundEvent;
  turnNumber: number;
}

export interface TeamMessageEvent extends Message {
  type: MessageType.TeamMessageEvent;
  turnNumber: number;
  message: string;
  messageType: string;
  senderId: number;
}

/** SkippedTurnEvent (1.12) */
export interface SkippedTurnEvent extends Message {
  type: MessageType.SkippedTurnEvent;
  turnNumber: number;
}

/** Union of all in-tick event types */
export type TickEvent =
  | BotDeathEvent
  | BotHitBotEvent
  | BotHitWallEvent
  | BulletFiredEvent
  | BulletHitBotEvent
  | BulletHitBulletEvent
  | BulletHitWallEvent
  | ScannedBotEvent
  | WonRoundEvent
  | TeamMessageEvent
  | SkippedTurnEvent;

// ---------------------------------------------------------------------------
// Top-level message interfaces (1.2–1.10)
// ---------------------------------------------------------------------------

/** ServerHandshake (1.2) */
export interface ServerHandshake extends Message {
  type: MessageType.ServerHandshake;
  sessionId: string;
  name: string;
  variant: string;
  version: string;
  gameTypes: string[];
  gameSetup?: GameSetup | null;
}

/** BotHandshake (1.3) */
export interface BotHandshake extends Message {
  type: MessageType.BotHandshake;
  sessionId: string;
  name: string;
  version: string;
  authors: string[];
  description?: string | null;
  homepage?: string | null;
  countryCodes?: string[] | null;
  gameTypes?: string[] | null;
  platform?: string | null;
  programmingLang?: string | null;
  initialPosition?: InitialPosition | null;
  teamId?: number | null;
  teamName?: string | null;
  teamVersion?: string | null;
  isDroid?: boolean | null;
  secret?: string | null;
  debuggerAttached?: boolean | null;
}

/** BotReady (1.4) */
export interface BotReady extends Message {
  type: MessageType.BotReady;
}

/** BotIntent (1.5) */
export interface BotIntent extends Message {
  type: MessageType.BotIntent;
  turnRate?: number | null;
  gunTurnRate?: number | null;
  radarTurnRate?: number | null;
  targetSpeed?: number | null;
  firepower?: number | null;
  adjustGunForBodyTurn?: boolean | null;
  adjustRadarForBodyTurn?: boolean | null;
  adjustRadarForGunTurn?: boolean | null;
  rescan?: boolean | null;
  fireAssist?: boolean | null;
  bodyColor?: string | null;
  turretColor?: string | null;
  radarColor?: string | null;
  bulletColor?: string | null;
  scanColor?: string | null;
  tracksColor?: string | null;
  gunColor?: string | null;
  stdOut?: string | null;
  stdErr?: string | null;
  teamMessages?: TeamMessage[] | null;
  debugGraphics?: string | null;
}

/** TickEventForBot (1.6) */
export interface TickEventForBot extends Message {
  type: MessageType.TickEventForBot;
  turnNumber: number;
  roundNumber: number;
  botState: BotState;
  bulletStates: BulletState[];
  events: TickEvent[];
}

/** GameStartedEventForBot (1.7) */
export interface GameStartedEventForBot extends Message {
  type: MessageType.GameStartedEventForBot;
  myId: number;
  startX?: number | null;
  startY?: number | null;
  startDirection?: number | null;
  teammateIds?: number[] | null;
  gameSetup: GameSetup;
}

/** GameEndedEventForBot (1.8) */
export interface GameEndedEventForBot extends Message {
  type: MessageType.GameEndedEventForBot;
  numberOfRounds: number;
  results: ResultsForBot;
}

/** RoundStartedEvent (1.9) */
export interface RoundStartedEvent extends Message {
  type: MessageType.RoundStartedEvent;
  roundNumber: number;
}

/** RoundEndedEventForBot (1.10) */
export interface RoundEndedEventForBot extends Message {
  type: MessageType.RoundEndedEventForBot;
  roundNumber: number;
  turnNumber: number;
  results: ResultsForBot;
}

/** GameAbortedEvent */
export interface GameAbortedEvent extends Message {
  type: MessageType.GameAbortedEvent;
}
