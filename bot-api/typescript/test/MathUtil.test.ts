import { describe, it, expect } from "vitest";
import { MathUtil } from "../src/util/MathUtil.js";

describe("LEGACY", () => {

describe("MathUtil", () => {
  describe("clamp", () => {
    it("returns value when within range", () => {
      expect(MathUtil.clamp(5, 0, 10)).toBe(5);
    });

    it("returns min when value is below range", () => {
      expect(MathUtil.clamp(-1, 0, 10)).toBe(0);
    });

    it("returns max when value is above range", () => {
      expect(MathUtil.clamp(11, 0, 10)).toBe(10);
    });

    it("returns min when value equals min", () => {
      expect(MathUtil.clamp(0, 0, 10)).toBe(0);
    });

    it("returns max when value equals max", () => {
      expect(MathUtil.clamp(10, 0, 10)).toBe(10);
    });

    it("works with negative range", () => {
      expect(MathUtil.clamp(-5, -10, -1)).toBe(-5);
    });

    it("works with decimal values", () => {
      expect(MathUtil.clamp(1.5, 1.0, 2.0)).toBe(1.5);
    });
  });
});
});
