import { describe, it, expect } from "vitest";
import { BotState } from "../src/BotState.js";
import { BotResults } from "../src/BotResults.js";
import { GameSetup } from "../src/GameSetup.js";
import { Color } from "../src/graphics/Color.js";

describe("TR-API-MDL-002: BotState constructor", () => {
  it("should store all fields correctly", () => {
    const bodyColor = Color.fromRgb(0x11, 0x11, 0x11);
    const turretColor = Color.fromRgb(0x22, 0x22, 0x22);
    const radarColor = Color.fromRgb(0x33, 0x33, 0x33);
    const bulletColor = Color.fromRgb(0x44, 0x44, 0x44);
    const scanColor = Color.fromRgb(0x55, 0x55, 0x55);
    const tracksColor = Color.fromRgb(0x66, 0x66, 0x66);
    const gunColor = Color.fromRgb(0x77, 0x77, 0x77);

    const state = new BotState(
      true, 100.0, 50.0, 60.0, 45.0, 90.0, 135.0, 5.0, 1.0, 2.0, 3.0, 4.0, 0.5, 3,
      bodyColor, turretColor, radarColor, bulletColor, scanColor, tracksColor, gunColor, true
    );

    expect(state.isDroid).toBe(true);
    expect(state.energy).toBe(100.0);
    expect(state.x).toBe(50.0);
    expect(state.y).toBe(60.0);
    expect(state.direction).toBe(45.0);
    expect(state.gunDirection).toBe(90.0);
    expect(state.radarDirection).toBe(135.0);
    expect(state.radarSweep).toBe(5.0);
    expect(state.speed).toBe(1.0);
    expect(state.turnRate).toBe(2.0);
    expect(state.gunTurnRate).toBe(3.0);
    expect(state.radarTurnRate).toBe(4.0);
    expect(state.gunHeat).toBe(0.5);
    expect(state.enemyCount).toBe(3);
    expect(state.bodyColor).toEqual(bodyColor);
    expect(state.turretColor).toEqual(turretColor);
    expect(state.radarColor).toEqual(radarColor);
    expect(state.bulletColor).toEqual(bulletColor);
    expect(state.scanColor).toEqual(scanColor);
    expect(state.tracksColor).toEqual(tracksColor);
    expect(state.gunColor).toEqual(gunColor);
    expect(state.isDebuggingEnabled).toBe(true);
  });
});

describe("TR-API-MDL-003: BotResults constructor", () => {
  it("should store all fields correctly", () => {
    const results = new BotResults(1, 100.0, 50.0, 30.0, 20.0, 10.0, 5.0, 215.0, 3, 2, 4);

    expect(results.rank).toBe(1);
    expect(results.survival).toBe(100.0);
    expect(results.lastSurvivorBonus).toBe(50.0);
    expect(results.bulletDamage).toBe(30.0);
    expect(results.bulletKillBonus).toBe(20.0);
    expect(results.ramDamage).toBe(10.0);
    expect(results.ramKillBonus).toBe(5.0);
    expect(results.totalScore).toBe(215.0);
    expect(results.firstPlaces).toBe(3);
    expect(results.secondPlaces).toBe(2);
    expect(results.thirdPlaces).toBe(4);
  });
});

describe("TR-API-MDL-004: GameSetup constructor", () => {
  it("should store all fields correctly", () => {
    const setup = new GameSetup("classic", 800, 600, 10, 0.1, 450, 30000, 1000);

    expect(setup.gameType).toBe("classic");
    expect(setup.arenaWidth).toBe(800);
    expect(setup.arenaHeight).toBe(600);
    expect(setup.numberOfRounds).toBe(10);
    expect(setup.gunCoolingRate).toBe(0.1);
    expect(setup.maxInactivityTurns).toBe(450);
    expect(setup.turnTimeout).toBe(30000);
    expect(setup.readyTimeout).toBe(1000);
  });
});
