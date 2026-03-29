import { describe, it, expect } from "vitest";
import { BaseBot } from "../src/BaseBot.js";
import { BotInfo } from "../src/BotInfo.js";
import { Color } from "../src/graphics/Color.js";
import { Condition } from "../src/events/Condition.js";

// Minimal concrete subclass for testing
class TestBot extends BaseBot {
  constructor(botInfo: BotInfo) {
    super(botInfo);
  }
}

const makeBotInfo = () =>
  new BotInfo("TestBot", "1.0", ["Author"], null, null, [], [], null, null, null);

const makeBot = () => new TestBot(makeBotInfo());

// ---------------------------------------------------------------------------
// Task 1: IBaseBot interface
// ---------------------------------------------------------------------------

describe("IBaseBot interface (task 1)", () => {
  it("1.13 TEAM_MESSAGE_MAX_SIZE constant is 32768", () => {
    const bot = makeBot();
    expect(bot.TEAM_MESSAGE_MAX_SIZE).toBe(32768);
  });

  it("1.13 MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN constant is 10", () => {
    const bot = makeBot();
    expect(bot.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN).toBe(10);
  });

  it("1.1 state accessors return default values", () => {
    const bot = makeBot();
    expect(bot.getMyId()).toBe(0);
    expect(bot.getVariant()).toBe("Tank Royale");
    expect(bot.getVersion()).toBe("");
    expect(bot.getGameType()).toBe("");
    expect(bot.getArenaWidth()).toBe(0);
    expect(bot.getArenaHeight()).toBe(0);
    expect(bot.getNumberOfRounds()).toBe(0);
    expect(bot.getGunCoolingRate()).toBe(0);
    expect(bot.getMaxInactivityTurns()).toBe(0);
    expect(bot.getTurnTimeout()).toBe(0);
    expect(bot.getTimeLeft()).toBe(0);
    expect(bot.getRoundNumber()).toBe(0);
    expect(bot.getTurnNumber()).toBe(0);
    expect(bot.getEnemyCount()).toBe(0);
    expect(bot.getEnergy()).toBe(0);
    expect(bot.isDisabled()).toBe(false);
    expect(bot.getX()).toBe(0);
    expect(bot.getY()).toBe(0);
    expect(bot.getDirection()).toBe(0);
    expect(bot.getGunDirection()).toBe(0);
    expect(bot.getRadarDirection()).toBe(0);
    expect(bot.getSpeed()).toBe(0);
    expect(bot.getGunHeat()).toBe(0);
  });

  it("1.1 getBulletStates returns empty set", () => {
    const bot = makeBot();
    expect(bot.getBulletStates().size).toBe(0);
  });

  it("1.1 getEvents returns empty array, clearEvents works", () => {
    const bot = makeBot();
    expect(bot.getEvents()).toEqual([]);
    bot.clearEvents();
    expect(bot.getEvents()).toEqual([]);
  });

  it("1.2 rate getters/setters work", () => {
    const bot = makeBot();
    bot.setTurnRate(5);
    expect(bot.getTurnRate()).toBe(5);
    bot.setMaxTurnRate(8);
    expect(bot.getMaxTurnRate()).toBe(8);
    bot.setGunTurnRate(10);
    expect(bot.getGunTurnRate()).toBe(10);
    bot.setMaxGunTurnRate(15);
    expect(bot.getMaxGunTurnRate()).toBe(15);
    bot.setRadarTurnRate(20);
    expect(bot.getRadarTurnRate()).toBe(20);
    bot.setMaxRadarTurnRate(30);
    expect(bot.getMaxRadarTurnRate()).toBe(30);
    bot.setTargetSpeed(4);
    expect(bot.getTargetSpeed()).toBe(4);
    bot.setMaxSpeed(6);
    expect(bot.getMaxSpeed()).toBe(6);
  });

  it("1.3 setFire returns false when gun is cold but firepower out of range", () => {
    const bot = makeBot();
    expect(bot.setFire(0)).toBe(false);
    expect(bot.setFire(4)).toBe(false);
  });

  it("1.3 setFire returns true for valid firepower when gun is cold", () => {
    const bot = makeBot();
    expect(bot.setFire(1)).toBe(true);
    expect(bot.getFirepower()).toBe(1);
  });

  it("1.4 adjustment flags default to false and can be set", () => {
    const bot = makeBot();
    expect(bot.isAdjustGunForBodyTurn()).toBe(false);
    bot.setAdjustGunForBodyTurn(true);
    expect(bot.isAdjustGunForBodyTurn()).toBe(true);

    expect(bot.isAdjustRadarForBodyTurn()).toBe(false);
    bot.setAdjustRadarForBodyTurn(true);
    expect(bot.isAdjustRadarForBodyTurn()).toBe(true);

    expect(bot.isAdjustRadarForGunTurn()).toBe(false);
    bot.setAdjustRadarForGunTurn(true);
    expect(bot.isAdjustRadarForGunTurn()).toBe(true);
  });

  it("1.5 setStop/setResume/isStopped work", () => {
    const bot = makeBot();
    expect(bot.isStopped()).toBe(false);
    bot.setStop();
    expect(bot.isStopped()).toBe(true);
    bot.setResume();
    expect(bot.isStopped()).toBe(false);
    bot.setStop(true);
    expect(bot.isStopped()).toBe(true);
  });

  it("1.6 setRescan does not throw", () => {
    const bot = makeBot();
    expect(() => bot.setRescan()).not.toThrow();
  });

  it("1.7 addCustomEvent/removeCustomEvent work", () => {
    const bot = makeBot();
    const cond = new Condition("test", () => false);
    expect(bot.addCustomEvent(cond)).toBe(true);
    expect(bot.removeCustomEvent(cond)).toBe(true);
  });

  it("1.8 setInterruptible does not throw", () => {
    const bot = makeBot();
    expect(() => bot.setInterruptible(true)).not.toThrow();
  });

  it("1.9 color getters return null by default, setters work", () => {
    const bot = makeBot();
    expect(bot.getBodyColor()).toBeNull();
    const red = Color.fromRgb(255, 0, 0);
    bot.setBodyColor(red);
    expect(bot.getBodyColor()).toEqual(red);

    bot.setTurretColor(red);
    expect(bot.getTurretColor()).toEqual(red);
    bot.setRadarColor(red);
    expect(bot.getRadarColor()).toEqual(red);
    bot.setBulletColor(red);
    expect(bot.getBulletColor()).toEqual(red);
    bot.setScanColor(red);
    expect(bot.getScanColor()).toEqual(red);
    bot.setTracksColor(red);
    expect(bot.getTracksColor()).toEqual(red);
    bot.setGunColor(red);
    expect(bot.getGunColor()).toEqual(red);
  });

  it("1.10 team methods work", () => {
    const bot = makeBot();
    expect(bot.getTeammateIds().size).toBe(0);
    expect(bot.isTeammate(42)).toBe(false);
    expect(() => bot.broadcastTeamMessage("hello")).not.toThrow();
    expect(() => bot.sendTeamMessage(1, "hello")).not.toThrow();
  });

  it("1.11 setFireAssist does not throw", () => {
    const bot = makeBot();
    expect(() => bot.setFireAssist(true)).not.toThrow();
  });

  it("1.12 start() does not throw synchronously (async internally)", () => {
    const bot = makeBot();
    expect(() => bot.start()).not.toThrow();
  });
});

// ---------------------------------------------------------------------------
// Task 2: IBot interface (structural check via BaseBot)
// ---------------------------------------------------------------------------

describe("IBot interface (task 2)", () => {
  it("2.1 IBot extends IBaseBot — BaseBot satisfies IBaseBot", () => {
    const bot = makeBot();
    // If this compiles and runs, the interface is satisfied
    expect(typeof bot.start).toBe("function");
    expect(typeof bot.go).toBe("function");
  });
});

// ---------------------------------------------------------------------------
// Task 3: BaseBot implementation
// ---------------------------------------------------------------------------

describe("BaseBot implementation (task 3)", () => {
  it("3.2 constructor with BotInfo works", () => {
    const info = makeBotInfo();
    const bot = new TestBot(info);
    expect(bot).toBeDefined();
  });

  it("3.2 constructor with BotInfo and serverUrl works", () => {
    class TestBot2 extends BaseBot {
      constructor() {
        super(makeBotInfo(), new URL("ws://localhost:7654"));
      }
    }
    expect(() => new TestBot2()).not.toThrow();
  });

  it("3.2 constructor with BotInfo, serverUrl, and serverSecret works", () => {
    class TestBot3 extends BaseBot {
      constructor() {
        super(makeBotInfo(), new URL("ws://localhost:7654"), "secret");
      }
    }
    expect(() => new TestBot3()).not.toThrow();
  });

  it("3.7 event handler methods are overridable and no-op by default", () => {
    const bot = makeBot();
    // All handlers should be callable without throwing
    expect(() => bot.onConnected({} as any)).not.toThrow();
    expect(() => bot.onDisconnected({} as any)).not.toThrow();
    expect(() => bot.onConnectionError({} as any)).not.toThrow();
    expect(() => bot.onGameStarted({} as any)).not.toThrow();
    expect(() => bot.onGameEnded({} as any)).not.toThrow();
    expect(() => bot.onRoundStarted({} as any)).not.toThrow();
    expect(() => bot.onRoundEnded({} as any)).not.toThrow();
    expect(() => bot.onTick({} as any)).not.toThrow();
    expect(() => bot.onBotDeath({} as any)).not.toThrow();
    expect(() => bot.onDeath({} as any)).not.toThrow();
    expect(() => bot.onHitBot({} as any)).not.toThrow();
    expect(() => bot.onHitWall({} as any)).not.toThrow();
    expect(() => bot.onBulletFired({} as any)).not.toThrow();
    expect(() => bot.onHitByBullet({} as any)).not.toThrow();
    expect(() => bot.onBulletHitBot({} as any)).not.toThrow();
    expect(() => bot.onBulletHitBullet({} as any)).not.toThrow();
    expect(() => bot.onBulletHitWall({} as any)).not.toThrow();
    expect(() => bot.onScannedBot({} as any)).not.toThrow();
    expect(() => bot.onSkippedTurn({} as any)).not.toThrow();
    expect(() => bot.onWonRound({} as any)).not.toThrow();
    expect(() => bot.onCustomEvent({} as any)).not.toThrow();
    expect(() => bot.onTeamMessage({} as any)).not.toThrow();
  });

  it("3.8 calcBearing returns relative angle from bot direction", () => {
    const bot = makeBot();
    // direction is 0, so bearing to 90 degrees = 90
    expect(bot.calcBearing(90)).toBeCloseTo(90);
    // bearing to 270 degrees = -90 (normalized relative)
    expect(bot.calcBearing(270)).toBeCloseTo(-90);
  });

  it("3.8 calcGunBearing returns relative angle from gun direction", () => {
    const bot = makeBot();
    expect(bot.calcGunBearing(45)).toBeCloseTo(45);
  });

  it("3.8 calcRadarBearing returns relative angle from radar direction", () => {
    const bot = makeBot();
    expect(bot.calcRadarBearing(180)).toBeCloseTo(180);
  });

  it("3.8 normalizeAbsoluteAngle normalizes to [0, 360)", () => {
    const bot = makeBot();
    expect(bot.normalizeAbsoluteAngle(0)).toBeCloseTo(0);
    expect(bot.normalizeAbsoluteAngle(360)).toBeCloseTo(0);
    expect(bot.normalizeAbsoluteAngle(-90)).toBeCloseTo(270);
    expect(bot.normalizeAbsoluteAngle(450)).toBeCloseTo(90);
  });

  it("3.8 normalizeRelativeAngle normalizes to (-180, 180]", () => {
    const bot = makeBot();
    expect(bot.normalizeRelativeAngle(0)).toBeCloseTo(0);
    expect(bot.normalizeRelativeAngle(180)).toBeCloseTo(180);
    expect(bot.normalizeRelativeAngle(-180)).toBeCloseTo(180);
    expect(bot.normalizeRelativeAngle(270)).toBeCloseTo(-90);
    expect(bot.normalizeRelativeAngle(-270)).toBeCloseTo(90);
  });

  it("3.8 calcDeltaAngle returns delta in [-180, 180]", () => {
    const bot = makeBot();
    expect(bot.calcDeltaAngle(90, 0)).toBeCloseTo(90);
    expect(bot.calcDeltaAngle(0, 90)).toBeCloseTo(-90);
    expect(bot.calcDeltaAngle(10, 350)).toBeCloseTo(20);
    expect(bot.calcDeltaAngle(350, 10)).toBeCloseTo(-20);
  });

  it("3.8 directionTo computes correct direction", () => {
    const bot = makeBot();
    // bot at (0,0), target at (0,100) => direction 0 (north)
    expect(bot.directionTo(0, 100)).toBeCloseTo(0);
    // target at (100,0) => direction 90 (east)
    expect(bot.directionTo(100, 0)).toBeCloseTo(90);
  });

  it("3.8 distanceTo computes Euclidean distance", () => {
    const bot = makeBot();
    expect(bot.distanceTo(3, 4)).toBeCloseTo(5);
    expect(bot.distanceTo(0, 0)).toBeCloseTo(0);
  });

  it("3.8 calcMaxTurnRate decreases with speed", () => {
    const bot = makeBot();
    expect(bot.calcMaxTurnRate(0)).toBeCloseTo(10);
    expect(bot.calcMaxTurnRate(8)).toBeCloseTo(4);
  });

  it("3.8 calcBulletSpeed = 20 - 3 * firepower", () => {
    const bot = makeBot();
    expect(bot.calcBulletSpeed(1)).toBeCloseTo(17);
    expect(bot.calcBulletSpeed(3)).toBeCloseTo(11);
  });

  it("3.8 calcGunHeat = 1 + firepower / 5", () => {
    const bot = makeBot();
    expect(bot.calcGunHeat(1)).toBeCloseTo(1.2);
    expect(bot.calcGunHeat(3)).toBeCloseTo(1.6);
  });

  it("3.5/3.6 getEventPriority/setEventPriority work", () => {
    const bot = makeBot();
    bot.setEventPriority("ScannedBotEvent", 99);
    expect(bot.getEventPriority("ScannedBotEvent")).toBe(99);
  });

  it("3.5 isDebuggingEnabled returns false by default", () => {
    const bot = makeBot();
    expect(bot.isDebuggingEnabled()).toBe(false);
  });
});
