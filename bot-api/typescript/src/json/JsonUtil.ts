import { MessageType } from "../protocol/MessageType.js";
import {
  TickEvent,
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
  TickEventForBot,
} from "../protocol/schema.js";

/** Serializes a value to a JSON string. */
export function toJson(value: unknown): string {
  return JSON.stringify(value);
}

/** Deserializes a JSON string to the given type. */
export function fromJson<T>(json: string): T {
  return JSON.parse(json) as T;
}

/**
 * Deserializes a TickEventForBot from JSON, with type-discriminated event deserialization
 * matching Java's RuntimeTypeAdapterFactory behavior.
 */
export function parseTickEventForBot(json: string): TickEventForBot {
  const raw = JSON.parse(json) as TickEventForBot;
  const events = (raw.events ?? []).map(deserializeTickEvent);
  return { ...raw, events };
}

/** Deserializes a single tick event using the `type` discriminator field. */
function deserializeTickEvent(e: unknown): TickEvent {
  const obj = e as { type: string };
  switch (obj.type) {
    case MessageType.BotDeathEvent:
      return obj as BotDeathEvent;
    case MessageType.BotHitBotEvent:
      return obj as BotHitBotEvent;
    case MessageType.BotHitWallEvent:
      return obj as BotHitWallEvent;
    case MessageType.BulletFiredEvent:
      return obj as BulletFiredEvent;
    case MessageType.BulletHitBotEvent:
      return obj as BulletHitBotEvent;
    case MessageType.BulletHitBulletEvent:
      return obj as BulletHitBulletEvent;
    case MessageType.BulletHitWallEvent:
      return obj as BulletHitWallEvent;
    case MessageType.ScannedBotEvent:
      return obj as ScannedBotEvent;
    case MessageType.WonRoundEvent:
      return obj as WonRoundEvent;
    case MessageType.TeamMessageEvent:
      return obj as TeamMessageEvent;
    case MessageType.SkippedTurnEvent:
      return obj as SkippedTurnEvent;
    default:
      throw new Error(`Unknown tick event type: ${obj.type}`);
  }
}
