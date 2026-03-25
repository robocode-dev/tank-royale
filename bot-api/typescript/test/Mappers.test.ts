import { describe, it, expect } from "vitest";
import { BulletStateMapper } from "../src/mapper/BulletStateMapper.js";
import { BotStateMapper } from "../src/mapper/BotStateMapper.js";
import { GameSetupMapper } from "../src/mapper/GameSetupMapper.js";
import { ResultsMapper } from "../src/mapper/ResultsMapper.js";
import { InitialPositionMapper } from "../src/mapper/InitialPositionMapper.js";
import { EventMapper } from "../src/mapper/EventMapper.js";
import { MessageType } from "../src/protocol/MessageType.js";
import {
  BotState as SchemaBotState,
  BulletState as SchemaBulletState,
  GameSetup as SchemaGameSetup,
  ResultsForBot,
  TickEventForBot,
  BotDeathEvent as SchemaBotDeathEvent,
  BulletHitBotEvent as SchemaBulletHitBotEvent,
  ScannedBotEvent as SchemaScannedBotEvent,
  WonRoundEvent as SchemaWonRoundEvent,
} from "../src/protocol/schema.js";
import { InitialPosition } from "../src/InitialPosition.js";
import { DeathEvent } from "../src/events/DeathEvent.js";
import { BotDeathEvent } from "../src/events/BotDeathEvent.js";
import { HitByBulletEvent } from "../src/events/HitByBulletEvent.js";
import { BulletHitBotEvent } from "../src/events/BulletHitBotEvent.js";
import { ScannedBotEvent } from "../src/events/ScannedBotEvent.js";
import { WonRoundEvent } from "../src/events/WonRoundEvent.js";

// ---------------------------------------------------------------------------
// Shared fixtures
// ---------------------------------------------------------------------------

const schemaBullet: SchemaBulletState = {
  bulletId: 1,
  ownerId: 10,
  power: 2.5,
  x: 100,
  y: 200,
  direction: 45,
  color: "#ff0000",
};

const schemaBotState: SchemaBotState = {
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
  bodyColor: "#aabbcc",
  turretColor: "#112233",
  radarColor: "#445566",
  bulletColor: "#778899",
  scanColor: "#aabbcc",
  tracksColor: "#ddeeff",
  gunColor: "#001122",
  isDebuggingEnabled: true,
};

const schemaGameSetup: SchemaGameSetup = {
  gameType: "classic",
  arenaWidth: 800,
  arenaHeight: 600,
  numberOfRounds: 10,
  gunCoolingRate: 0.1,
  maxInactivityTurns: 450,
  turnTimeout: 30000,
  readyTimeout: 1000000,
};

const schemaResults: ResultsForBot = {
  rank: 1,
  survival: 50,
  lastSurvivorBonus: 10,
  bulletDamage: 30,
  bulletKillBonus: 20,
  ramDamage: 5,
  ramKillBonus: 2,
  totalScore: 117,
  firstPlaces: 3,
  secondPlaces: 1,
  thirdPlaces: 0,
};

// ---------------------------------------------------------------------------
// BulletStateMapper
// ---------------------------------------------------------------------------

describe("BulletStateMapper", () => {
  it("maps all fields correctly", () => {
    const result = BulletStateMapper.map(schemaBullet);
    expect(result.bulletId).toBe(1);
    expect(result.ownerId).toBe(10);
    expect(result.power).toBe(2.5);
    expect(result.x).toBe(100);
    expect(result.y).toBe(200);
    expect(result.direction).toBe(45);
    expect(result.color).toBe("#ff0000");
  });

  it("maps null color to null", () => {
    const result = BulletStateMapper.map({ ...schemaBullet, color: undefined });
    expect(result.color).toBeNull();
  });

  it("mapCollection maps all items", () => {
    const results = BulletStateMapper.mapCollection([schemaBullet, schemaBullet]);
    expect(results).toHaveLength(2);
    expect(results[0].bulletId).toBe(1);
  });
});

// ---------------------------------------------------------------------------
// BotStateMapper
// ---------------------------------------------------------------------------

describe("BotStateMapper", () => {
  it("maps all fields correctly", () => {
    const result = BotStateMapper.map(schemaBotState);
    expect(result.isDroid).toBe(false);
    expect(result.energy).toBe(100);
    expect(result.x).toBe(50);
    expect(result.y).toBe(60);
    expect(result.direction).toBe(90);
    expect(result.gunDirection).toBe(180);
    expect(result.radarDirection).toBe(270);
    expect(result.radarSweep).toBe(45);
    expect(result.speed).toBe(8);
    expect(result.turnRate).toBe(10);
    expect(result.gunTurnRate).toBe(20);
    expect(result.radarTurnRate).toBe(45);
    expect(result.gunHeat).toBe(0.5);
    expect(result.enemyCount).toBe(3);
    expect(result.bodyColor).toBe("#aabbcc");
    expect(result.turretColor).toBe("#112233");
    expect(result.radarColor).toBe("#445566");
    expect(result.bulletColor).toBe("#778899");
    expect(result.scanColor).toBe("#aabbcc");
    expect(result.tracksColor).toBe("#ddeeff");
    expect(result.gunColor).toBe("#001122");
    expect(result.isDebuggingEnabled).toBe(true);
  });

  it("maps undefined colors to null", () => {
    const result = BotStateMapper.map({ ...schemaBotState, bodyColor: undefined, gunColor: undefined });
    expect(result.bodyColor).toBeNull();
    expect(result.gunColor).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// GameSetupMapper
// ---------------------------------------------------------------------------

describe("GameSetupMapper", () => {
  it("maps all fields correctly", () => {
    const result = GameSetupMapper.map(schemaGameSetup);
    expect(result.gameType).toBe("classic");
    expect(result.arenaWidth).toBe(800);
    expect(result.arenaHeight).toBe(600);
    expect(result.numberOfRounds).toBe(10);
    expect(result.gunCoolingRate).toBe(0.1);
    expect(result.maxInactivityTurns).toBe(450);
    expect(result.turnTimeout).toBe(30000);
    expect(result.readyTimeout).toBe(1000000);
  });
});

// ---------------------------------------------------------------------------
// ResultsMapper
// ---------------------------------------------------------------------------

describe("ResultsMapper", () => {
  it("maps all fields correctly", () => {
    const result = ResultsMapper.map(schemaResults);
    expect(result.rank).toBe(1);
    expect(result.survival).toBe(50);
    expect(result.lastSurvivorBonus).toBe(10);
    expect(result.bulletDamage).toBe(30);
    expect(result.bulletKillBonus).toBe(20);
    expect(result.ramDamage).toBe(5);
    expect(result.ramKillBonus).toBe(2);
    expect(result.totalScore).toBe(117);
    expect(result.firstPlaces).toBe(3);
    expect(result.secondPlaces).toBe(1);
    expect(result.thirdPlaces).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// InitialPositionMapper
// ---------------------------------------------------------------------------

describe("InitialPositionMapper", () => {
  it("maps all fields correctly", () => {
    const pos = new InitialPosition(10, 20, 90);
    const result = InitialPositionMapper.map(pos);
    expect(result).not.toBeNull();
    expect(result!.x).toBe(10);
    expect(result!.y).toBe(20);
    expect(result!.direction).toBe(90);
  });

  it("returns null for null input", () => {
    expect(InitialPositionMapper.map(null)).toBeNull();
  });

  it("returns null for undefined input", () => {
    expect(InitialPositionMapper.map(undefined)).toBeNull();
  });

  it("maps null fields through", () => {
    const pos = new InitialPosition(null, null, null);
    const result = InitialPositionMapper.map(pos);
    expect(result).not.toBeNull();
    expect(result!.x).toBeNull();
    expect(result!.y).toBeNull();
    expect(result!.direction).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// EventMapper
// ---------------------------------------------------------------------------

function makeTick(events: TickEventForBot["events"]): TickEventForBot {
  return {
    type: MessageType.TickEventForBot,
    turnNumber: 5,
    roundNumber: 2,
    botState: schemaBotState,
    bulletStates: [schemaBullet],
    events,
  };
}

describe("EventMapper", () => {
  const myBotId = 42;

  it("maps tick metadata correctly", () => {
    const tick = EventMapper.map(makeTick([]), myBotId);
    expect(tick.turnNumber).toBe(5);
    expect(tick.roundNumber).toBe(2);
    expect(tick.bulletStates).toHaveLength(1);
    expect(tick.events).toHaveLength(0);
  });

  it("maps BotDeathEvent with victimId == myBotId to DeathEvent", () => {
    const ev: SchemaBotDeathEvent = { type: MessageType.BotDeathEvent, turnNumber: 5, victimId: myBotId };
    const tick = EventMapper.map(makeTick([ev]), myBotId);
    expect(tick.events[0]).toBeInstanceOf(DeathEvent);
    expect((tick.events[0] as DeathEvent).isCritical).toBe(true);
  });

  it("maps BotDeathEvent with other victimId to BotDeathEvent", () => {
    const ev: SchemaBotDeathEvent = { type: MessageType.BotDeathEvent, turnNumber: 5, victimId: 99 };
    const tick = EventMapper.map(makeTick([ev]), myBotId);
    expect(tick.events[0]).toBeInstanceOf(BotDeathEvent);
    expect((tick.events[0] as BotDeathEvent).victimId).toBe(99);
  });

  it("maps BulletHitBotEvent with victimId == myBotId to HitByBulletEvent", () => {
    const ev: SchemaBulletHitBotEvent = {
      type: MessageType.BulletHitBotEvent,
      turnNumber: 5,
      victimId: myBotId,
      bullet: schemaBullet,
      damage: 10,
      energy: 90,
    };
    const tick = EventMapper.map(makeTick([ev]), myBotId);
    expect(tick.events[0]).toBeInstanceOf(HitByBulletEvent);
  });

  it("maps BulletHitBotEvent with other victimId to BulletHitBotEvent", () => {
    const ev: SchemaBulletHitBotEvent = {
      type: MessageType.BulletHitBotEvent,
      turnNumber: 5,
      victimId: 99,
      bullet: schemaBullet,
      damage: 10,
      energy: 90,
    };
    const tick = EventMapper.map(makeTick([ev]), myBotId);
    expect(tick.events[0]).toBeInstanceOf(BulletHitBotEvent);
    expect((tick.events[0] as BulletHitBotEvent).victimId).toBe(99);
  });

  it("maps ScannedBotEvent correctly", () => {
    const ev: SchemaScannedBotEvent = {
      type: MessageType.ScannedBotEvent,
      turnNumber: 5,
      scannedByBotId: 1,
      scannedBotId: 2,
      energy: 80,
      x: 300,
      y: 400,
      direction: 135,
      speed: 5,
    };
    const tick = EventMapper.map(makeTick([ev]), myBotId);
    const mapped = tick.events[0] as ScannedBotEvent;
    expect(mapped).toBeInstanceOf(ScannedBotEvent);
    expect(mapped.scannedByBotId).toBe(1);
    expect(mapped.scannedBotId).toBe(2);
    expect(mapped.energy).toBe(80);
    expect(mapped.x).toBe(300);
    expect(mapped.y).toBe(400);
    expect(mapped.direction).toBe(135);
    expect(mapped.speed).toBe(5);
  });

  it("maps WonRoundEvent correctly", () => {
    const ev: SchemaWonRoundEvent = { type: MessageType.WonRoundEvent, turnNumber: 5 };
    const tick = EventMapper.map(makeTick([ev]), myBotId);
    expect(tick.events[0]).toBeInstanceOf(WonRoundEvent);
  });
});
