import { describe, it, expect } from "vitest";
import { Constants } from "../src/Constants.js";

describe("LEGACY", () => {

describe("Constants", () => {
  it("BOUNDING_CIRCLE_RADIUS is 18", () => {
    expect(Constants.BOUNDING_CIRCLE_RADIUS).toBe(18);
  });

  it("SCAN_RADIUS is 1200", () => {
    expect(Constants.SCAN_RADIUS).toBe(1200);
  });

  it("MAX_TURN_RATE is 10", () => {
    expect(Constants.MAX_TURN_RATE).toBe(10);
  });

  it("MAX_GUN_TURN_RATE is 20", () => {
    expect(Constants.MAX_GUN_TURN_RATE).toBe(20);
  });

  it("MAX_RADAR_TURN_RATE is 45", () => {
    expect(Constants.MAX_RADAR_TURN_RATE).toBe(45);
  });

  it("MAX_SPEED is 8", () => {
    expect(Constants.MAX_SPEED).toBe(8);
  });

  it("MIN_FIREPOWER is 0.1", () => {
    expect(Constants.MIN_FIREPOWER).toBe(0.1);
  });

  it("MAX_FIREPOWER is 3", () => {
    expect(Constants.MAX_FIREPOWER).toBe(3);
  });

  it("MIN_BULLET_SPEED is 11 (20 - 3 * MAX_FIREPOWER)", () => {
    expect(Constants.MIN_BULLET_SPEED).toBe(11);
  });

  it("MAX_BULLET_SPEED is 19.7 (20 - 3 * MIN_FIREPOWER)", () => {
    expect(Constants.MAX_BULLET_SPEED).toBeCloseTo(19.7, 10);
  });

  it("ACCELERATION is 1", () => {
    expect(Constants.ACCELERATION).toBe(1);
  });

  it("DECELERATION is -2", () => {
    expect(Constants.DECELERATION).toBe(-2);
  });
});
});
