import { describe, it, expect } from "vitest";
import { BotState } from "../src/BotState.js";

describe("LEGACY", () => {

describe("BotState", () => {
  it("stores all 22 fields", () => {
    const state = new BotState(
      false, 100, 50, 75, 45, 90, 135, 30, 8, 5, 3, 2, 1.5, 3,
      "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF", "#FFFFFF",
      true,
    );
    expect(state.isDroid).toBe(false);
    expect(state.energy).toBe(100);
    expect(state.x).toBe(50);
    expect(state.y).toBe(75);
    expect(state.direction).toBe(45);
    expect(state.gunDirection).toBe(90);
    expect(state.radarDirection).toBe(135);
    expect(state.radarSweep).toBe(30);
    expect(state.speed).toBe(8);
    expect(state.turnRate).toBe(5);
    expect(state.gunTurnRate).toBe(3);
    expect(state.radarTurnRate).toBe(2);
    expect(state.gunHeat).toBe(1.5);
    expect(state.enemyCount).toBe(3);
    expect(state.bodyColor).toBe("#FF0000");
    expect(state.turretColor).toBe("#00FF00");
    expect(state.radarColor).toBe("#0000FF");
    expect(state.bulletColor).toBe("#FFFF00");
    expect(state.scanColor).toBe("#FF00FF");
    expect(state.tracksColor).toBe("#00FFFF");
    expect(state.gunColor).toBe("#FFFFFF");
    expect(state.isDebuggingEnabled).toBe(true);
  });

  it("allows null color fields", () => {
    const state = new BotState(
      true, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      null, null, null, null, null, null, null,
      false,
    );
    expect(state.bodyColor).toBeNull();
    expect(state.gunColor).toBeNull();
  });
});
});
