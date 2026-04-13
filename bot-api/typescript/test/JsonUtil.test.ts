import { describe, it, expect } from "vitest";
import { toJson, fromJson, parseTickEventForBot } from "../src/json/JsonUtil.js";
import { MessageType } from "../src/protocol/MessageType.js";
import { TickEventForBot, BotDeathEvent, ScannedBotEvent, WonRoundEvent } from "../src/protocol/schema.js";

const baseBotState = {
  isDroid: false,
  energy: 100,
  x: 50,
  y: 60,
  direction: 90,
  gunDirection: 180,
  radarDirection: 270,
  radarSweep: 45,
  speed: 8,
  turnRate: 10,
  gunTurnRate: 20,
  radarTurnRate: 45,
  gunHeat: 0.5,
  enemyCount: 3,
  isDebuggingEnabled: false,
};

const baseBullet = { bulletId: 1, ownerId: 10, power: 2.5, x: 100, y: 200, direction: 45 };

function makeTickJson(events: unknown[]): string {
  return JSON.stringify({
    type: MessageType.TickEventForBot,
    turnNumber: 3,
    roundNumber: 1,
    botState: baseBotState,
    bulletStates: [baseBullet],
    events,
  });
}

// ---------------------------------------------------------------------------
// toJson / fromJson
// ---------------------------------------------------------------------------

describe("toJson / fromJson", () => {
  it("round-trips a simple object", () => {
    const obj = { type: MessageType.BotReady };
    const json = toJson(obj);
    const result = fromJson<typeof obj>(json);
    expect(result.type).toBe(MessageType.BotReady);
  });

  it("round-trips a BotIntent message", () => {
    const intent = {
      type: MessageType.BotIntent,
      turnRate: 5,
      gunTurnRate: 10,
      firepower: 1.5,
    };
    const json = toJson(intent);
    const result = fromJson<typeof intent>(json);
    expect(result.type).toBe(MessageType.BotIntent);
    expect(result.turnRate).toBe(5);
    expect(result.firepower).toBe(1.5);
  });

  it("round-trips null values", () => {
    const obj = { type: MessageType.BotIntent, turnRate: null };
    const json = toJson(obj);
    const result = fromJson<typeof obj>(json);
    expect(result.turnRate).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// parseTickEventForBot — type-discriminated deserialization
// ---------------------------------------------------------------------------

describe("parseTickEventForBot", () => {
  it("parses tick with no events", () => {
    const tick = parseTickEventForBot(makeTickJson([]));
    expect(tick.turnNumber).toBe(3);
    expect(tick.roundNumber).toBe(1);
    expect(tick.events).toHaveLength(0);
  });

  it("deserializes BotDeathEvent by type discriminator", () => {
    const ev: BotDeathEvent = { type: MessageType.BotDeathEvent, turnNumber: 3, victimId: 7 };
    const tick = parseTickEventForBot(makeTickJson([ev]));
    expect(tick.events).toHaveLength(1);
    expect(tick.events[0].type).toBe(MessageType.BotDeathEvent);
    expect((tick.events[0] as BotDeathEvent).victimId).toBe(7);
  });

  it("deserializes ScannedBotEvent by type discriminator", () => {
    const ev: ScannedBotEvent = {
      type: MessageType.ScannedBotEvent,
      turnNumber: 3,
      scannedByBotId: 1,
      scannedBotId: 2,
      energy: 80,
      x: 300,
      y: 400,
      direction: 135,
      speed: 5,
    };
    const tick = parseTickEventForBot(makeTickJson([ev]));
    expect(tick.events[0].type).toBe(MessageType.ScannedBotEvent);
    expect((tick.events[0] as ScannedBotEvent).scannedBotId).toBe(2);
  });

  it("deserializes WonRoundEvent by type discriminator", () => {
    const ev: WonRoundEvent = { type: MessageType.WonRoundEvent, turnNumber: 3 };
    const tick = parseTickEventForBot(makeTickJson([ev]));
    expect(tick.events[0].type).toBe(MessageType.WonRoundEvent);
  });

  it("deserializes multiple mixed events", () => {
    const events = [
      { type: MessageType.BotDeathEvent, turnNumber: 3, victimId: 5 },
      { type: MessageType.WonRoundEvent, turnNumber: 3 },
      { type: MessageType.ScannedBotEvent, turnNumber: 3, scannedByBotId: 1, scannedBotId: 2, energy: 50, x: 0, y: 0, direction: 0, speed: 0 },
    ];
    const tick = parseTickEventForBot(makeTickJson(events));
    expect(tick.events).toHaveLength(3);
    expect(tick.events[0].type).toBe(MessageType.BotDeathEvent);
    expect(tick.events[1].type).toBe(MessageType.WonRoundEvent);
    expect(tick.events[2].type).toBe(MessageType.ScannedBotEvent);
  });

  it("throws on unknown event type", () => {
    const ev = { type: "UnknownEvent", turnNumber: 3 };
    expect(() => parseTickEventForBot(makeTickJson([ev]))).toThrow("Unknown tick event type: UnknownEvent");
  });
});
