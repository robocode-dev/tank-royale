/**
 * Protocol layer — wire-format DTOs and message types for the Tank Royale protocol.
 */
export { MessageType } from "./MessageType.js";
export type {
  Message,
  // Schema value types
  BotState,
  BulletState,
  GameSetup,
  ResultsForBot,
  InitialPosition,
  TeamMessage,
  // In-tick events
  BotDeathEvent,
  BotHitBotEvent,
  BotHitWallEvent,
  BulletFiredEvent,
  BulletHitBotEvent,
  BulletHitBulletEvent,
  BulletHitWallEvent,
  ScannedBotEvent,
  WonRoundEvent,
  TeamMessageEvent,
  SkippedTurnEvent,
  TickEvent,
  // Top-level messages
  ServerHandshake,
  BotHandshake,
  BotReady,
  BotIntent,
  TickEventForBot,
  GameStartedEventForBot,
  GameEndedEventForBot,
  RoundStartedEvent,
  RoundEndedEventForBot,
  GameAbortedEvent,
} from "./schema.js";
