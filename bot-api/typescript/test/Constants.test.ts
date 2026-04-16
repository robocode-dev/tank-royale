import { describe, it, expect } from "vitest";
import { Constants } from "../src/Constants.js";

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

  it("INACTIVITY_ZAP is 0.1", () => {
    expect(Constants.INACTIVITY_ZAP).toBe(0.1);
  });

  it("RAM_DAMAGE is 0.6", () => {
    expect(Constants.RAM_DAMAGE).toBe(0.6);
  });

  it("STARTING_GUN_HEAT is 3.0", () => {
    expect(Constants.STARTING_GUN_HEAT).toBe(3.0);
  });

  it("TEAM_MESSAGE_MAX_SIZE is 32768", () => {
    expect(Constants.TEAM_MESSAGE_MAX_SIZE).toBe(32768);
  });

  it("MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN is 10", () => {
    expect(Constants.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN).toBe(10);
  });
});
