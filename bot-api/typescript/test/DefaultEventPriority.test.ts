import { describe, it, expect } from "vitest";
import { DefaultEventPriority } from "../src/DefaultEventPriority.js";

describe("LEGACY", () => {

describe("DefaultEventPriority", () => {
  it("WON_ROUND is 150", () => {
    expect(DefaultEventPriority.WON_ROUND).toBe(150);
  });

  it("SKIPPED_TURN is 140", () => {
    expect(DefaultEventPriority.SKIPPED_TURN).toBe(140);
  });

  it("TICK is 130", () => {
    expect(DefaultEventPriority.TICK).toBe(130);
  });

  it("CUSTOM is 120", () => {
    expect(DefaultEventPriority.CUSTOM).toBe(120);
  });

  it("TEAM_MESSAGE is 110", () => {
    expect(DefaultEventPriority.TEAM_MESSAGE).toBe(110);
  });

  it("BOT_DEATH is 100", () => {
    expect(DefaultEventPriority.BOT_DEATH).toBe(100);
  });

  it("BULLET_HIT_WALL is 90", () => {
    expect(DefaultEventPriority.BULLET_HIT_WALL).toBe(90);
  });

  it("BULLET_HIT_BULLET is 80", () => {
    expect(DefaultEventPriority.BULLET_HIT_BULLET).toBe(80);
  });

  it("BULLET_HIT_BOT is 70", () => {
    expect(DefaultEventPriority.BULLET_HIT_BOT).toBe(70);
  });

  it("BULLET_FIRED is 60", () => {
    expect(DefaultEventPriority.BULLET_FIRED).toBe(60);
  });

  it("HIT_BY_BULLET is 50", () => {
    expect(DefaultEventPriority.HIT_BY_BULLET).toBe(50);
  });

  it("HIT_WALL is 40", () => {
    expect(DefaultEventPriority.HIT_WALL).toBe(40);
  });

  it("HIT_BOT is 30", () => {
    expect(DefaultEventPriority.HIT_BOT).toBe(30);
  });

  it("SCANNED_BOT is 20", () => {
    expect(DefaultEventPriority.SCANNED_BOT).toBe(20);
  });

  it("DEATH is 10", () => {
    expect(DefaultEventPriority.DEATH).toBe(10);
  });

  it("priorities are in descending order from WON_ROUND to DEATH", () => {
    const priorities = [
      DefaultEventPriority.WON_ROUND,
      DefaultEventPriority.SKIPPED_TURN,
      DefaultEventPriority.TICK,
      DefaultEventPriority.CUSTOM,
      DefaultEventPriority.TEAM_MESSAGE,
      DefaultEventPriority.BOT_DEATH,
      DefaultEventPriority.BULLET_HIT_WALL,
      DefaultEventPriority.BULLET_HIT_BULLET,
      DefaultEventPriority.BULLET_HIT_BOT,
      DefaultEventPriority.BULLET_FIRED,
      DefaultEventPriority.HIT_BY_BULLET,
      DefaultEventPriority.HIT_WALL,
      DefaultEventPriority.HIT_BOT,
      DefaultEventPriority.SCANNED_BOT,
      DefaultEventPriority.DEATH,
    ];
    for (let i = 1; i < priorities.length; i++) {
      expect(priorities[i]).toBeLessThan(priorities[i - 1]!);
    }
  });
});
});
