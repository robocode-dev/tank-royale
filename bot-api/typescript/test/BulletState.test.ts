import { describe, it, expect } from "vitest";
import { BulletState } from "../src/BulletState.js";

describe("LEGACY", () => {

describe("BulletState", () => {
  it("stores all fields", () => {
    const bullet = new BulletState(1, 2, 3.0, 100, 200, 45, "#FF0000");
    expect(bullet.bulletId).toBe(1);
    expect(bullet.ownerId).toBe(2);
    expect(bullet.power).toBe(3.0);
    expect(bullet.x).toBe(100);
    expect(bullet.y).toBe(200);
    expect(bullet.direction).toBe(45);
    expect(bullet.color).toBe("#FF0000");
  });

  it("computes speed as 20 - 3 * power", () => {
    expect(new BulletState(1, 1, 1.0, 0, 0, 0, null).speed).toBe(17);
    expect(new BulletState(1, 1, 2.0, 0, 0, 0, null).speed).toBe(14);
    expect(new BulletState(1, 1, 3.0, 0, 0, 0, null).speed).toBe(11);
    expect(new BulletState(1, 1, 0.1, 0, 0, 0, null).speed).toBeCloseTo(19.7);
  });

  it("allows null color", () => {
    const bullet = new BulletState(1, 1, 1.0, 0, 0, 0, null);
    expect(bullet.color).toBeNull();
  });
});
});
