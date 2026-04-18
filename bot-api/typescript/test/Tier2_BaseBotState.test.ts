import { describe, it, expect } from "vitest";
import { BaseBot } from "../src/BaseBot.js";
import { BotInfo } from "../src/BotInfo.js";
import { BotException } from "../src/BotException.js";

class TestBot extends BaseBot {
  constructor() {
    super(new BotInfo("TestBot", "1.0", ["Author"], null, null, null, ["classic"], null, null));
  }
  run() {}
}

describe("TR-API-BOT-007: BaseBot accessor defaults", () => {
  it("should throw BotException for state-dependent accessors when no state is available", () => {
    // LEGACY: Superseded by cross-platform basebot-defaults.json TR-API-BOT-007 JSON tests. Delete once all platforms are ✅.
    const bot = new TestBot();

    // Metadata accessors should throw BotException when not connected
    expect(() => bot.getMyId()).toThrow(BotException);
    expect(() => bot.getVariant()).toThrow(BotException);
    expect(() => bot.getVersion()).toThrow(BotException);

    // State-dependent accessors should throw BotException when no state is available
    expect(() => bot.getEnergy()).toThrow(BotException);
    expect(() => bot.getX()).toThrow(BotException);
    expect(() => bot.getY()).toThrow(BotException);
    expect(() => bot.getDirection()).toThrow(BotException);
    expect(() => bot.getGunDirection()).toThrow(BotException);
    expect(() => bot.getRadarDirection()).toThrow(BotException);
    expect(bot.getSpeed()).toBe(0);
    expect(bot.getGunHeat()).toBe(0);
    expect(bot.getBulletStates().size).toBe(0);
    expect(() => bot.getEvents()).toThrow(BotException);

    // Game setup accessors should throw BotException when no game setup is available
    expect(() => bot.getArenaWidth()).toThrow(BotException);
    expect(() => bot.getArenaHeight()).toThrow(BotException);
    expect(() => bot.getGameType()).toThrow(BotException);
  });
});

describe("TR-API-BOT-008: Adjustment flags default false", () => {
  it("should have adjustment flags defaulting to false", () => {
    // LEGACY: Superseded by cross-platform basebot-defaults.json TR-API-BOT-008 JSON tests. Delete once all platforms are ✅.
    const bot = new TestBot();

    expect(bot.isAdjustGunForBodyTurn()).toBe(false);
    expect(bot.isAdjustRadarForBodyTurn()).toBe(false);
    expect(bot.isAdjustRadarForGunTurn()).toBe(false);
  });
});
