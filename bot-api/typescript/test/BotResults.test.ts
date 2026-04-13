import { describe, it, expect } from "vitest";
import { BotResults } from "../src/BotResults.js";

describe("BotResults", () => {
  it("stores all fields", () => {
    const results = new BotResults(1, 200, 50, 300, 100, 10, 5, 665, 3, 1, 2);
    expect(results.rank).toBe(1);
    expect(results.survival).toBe(200);
    expect(results.lastSurvivorBonus).toBe(50);
    expect(results.bulletDamage).toBe(300);
    expect(results.bulletKillBonus).toBe(100);
    expect(results.ramDamage).toBe(10);
    expect(results.ramKillBonus).toBe(5);
    expect(results.totalScore).toBe(665);
    expect(results.firstPlaces).toBe(3);
    expect(results.secondPlaces).toBe(1);
    expect(results.thirdPlaces).toBe(2);
  });
});
