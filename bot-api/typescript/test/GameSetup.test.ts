import { describe, it, expect } from "vitest";
import { GameSetup } from "../src/GameSetup.js";

describe("GameSetup", () => {
  it("stores all fields", () => {
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
