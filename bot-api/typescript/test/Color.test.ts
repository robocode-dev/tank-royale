import { describe, it, expect } from "vitest";
import { Color } from "../src/graphics/Color";

describe("Color", () => {
  describe("fromRgb", () => {
    it("stores r, g, b with alpha=255", () => {
      const c = Color.fromRgb(10, 20, 30);
      expect(c.getR()).toBe(10);
      expect(c.getG()).toBe(20);
      expect(c.getB()).toBe(30);
      expect(c.getA()).toBe(255);
    });
  });

  describe("fromRgba (r,g,b,a)", () => {
    it("stores all four components", () => {
      const c = Color.fromRgba(1, 2, 3, 4);
      expect(c.getR()).toBe(1);
      expect(c.getG()).toBe(2);
      expect(c.getB()).toBe(3);
      expect(c.getA()).toBe(4);
    });
  });

  describe("fromRgba (packed int)", () => {
    it("unpacks components correctly", () => {
      const packed = (0xAB << 24 | 0xCD << 16 | 0xEF << 8 | 0x12) >>> 0;
      const c = Color.fromRgba(packed);
      expect(c.getR()).toBe(0xAB);
      expect(c.getG()).toBe(0xCD);
      expect(c.getB()).toBe(0xEF);
      expect(c.getA()).toBe(0x12);
    });
  });

  describe("fromRgba (baseColor, a)", () => {
    it("copies rgb from base and uses new alpha", () => {
      const base = Color.fromRgb(100, 150, 200);
      const c = Color.fromRgba(base, 128);
      expect(c.getR()).toBe(100);
      expect(c.getG()).toBe(150);
      expect(c.getB()).toBe(200);
      expect(c.getA()).toBe(128);
    });
  });

  describe("toRgba", () => {
    it("round-trips packed value", () => {
      const c = Color.fromRgba(10, 20, 30, 40);
      const packed = (10 << 24 | 20 << 16 | 30 << 8 | 40) >>> 0;
      expect(c.toRgba()).toBe(packed);
    });
  });

  describe("toHexColor", () => {
    it("returns #RRGGBB when alpha is 255", () => {
      expect(Color.fromRgb(255, 0, 0).toHexColor()).toBe("#FF0000");
      expect(Color.fromRgb(0, 255, 0).toHexColor()).toBe("#00FF00");
      expect(Color.fromRgb(0, 0, 255).toHexColor()).toBe("#0000FF");
    });

    it("returns #RRGGBBAA when alpha is not 255", () => {
      expect(Color.fromRgba(255, 0, 0, 128).toHexColor()).toBe("#FF000080");
    });

    it("pads single-digit hex values", () => {
      expect(Color.fromRgb(1, 2, 3).toHexColor()).toBe("#010203");
    });
  });

  describe("equals", () => {
    it("same value is equal", () => {
      const a = Color.fromRgb(10, 20, 30);
      const b = Color.fromRgb(10, 20, 30);
      expect(a.equals(b)).toBe(true);
    });

    it("different value is not equal", () => {
      expect(Color.fromRgb(1, 2, 3).equals(Color.fromRgb(1, 2, 4))).toBe(false);
    });

    it("same reference is equal", () => {
      const c = Color.RED;
      expect(c.equals(c)).toBe(true);
    });
  });

  describe("toString", () => {
    it("omits alpha when 255", () => {
      expect(Color.fromRgb(1, 2, 3).toString()).toBe("Color(r=1, g=2, b=3)");
    });

    it("includes alpha when not 255", () => {
      expect(Color.fromRgba(1, 2, 3, 4).toString()).toBe("Color(r=1, g=2, b=3, a=4)");
    });
  });

  describe("named constants", () => {
    it("RED is (255,0,0)", () => {
      expect(Color.RED.getR()).toBe(255);
      expect(Color.RED.getG()).toBe(0);
      expect(Color.RED.getB()).toBe(0);
      expect(Color.RED.getA()).toBe(255);
    });

    it("GREEN is (0,128,0)", () => {
      expect(Color.GREEN.getR()).toBe(0);
      expect(Color.GREEN.getG()).toBe(128);
      expect(Color.GREEN.getB()).toBe(0);
    });

    it("BLUE is (0,0,255)", () => {
      expect(Color.BLUE.getR()).toBe(0);
      expect(Color.BLUE.getG()).toBe(0);
      expect(Color.BLUE.getB()).toBe(255);
    });

    it("WHITE is (255,255,255)", () => {
      expect(Color.WHITE.toHexColor()).toBe("#FFFFFF");
    });

    it("BLACK is (0,0,0)", () => {
      expect(Color.BLACK.toHexColor()).toBe("#000000");
    });

    it("TRANSPARENT is (255,255,255,0)", () => {
      expect(Color.TRANSPARENT.getR()).toBe(255);
      expect(Color.TRANSPARENT.getG()).toBe(255);
      expect(Color.TRANSPARENT.getB()).toBe(255);
      expect(Color.TRANSPARENT.getA()).toBe(0);
    });

    it("YELLOW is (255,255,0)", () => {
      expect(Color.YELLOW.getR()).toBe(255);
      expect(Color.YELLOW.getG()).toBe(255);
      expect(Color.YELLOW.getB()).toBe(0);
    });

    it("YELLOW_GREEN is (154,205,50)", () => {
      expect(Color.YELLOW_GREEN.getR()).toBe(154);
      expect(Color.YELLOW_GREEN.getG()).toBe(205);
      expect(Color.YELLOW_GREEN.getB()).toBe(50);
    });

    it("MEDIUM_VIOLET_RED is (199,21,133)", () => {
      expect(Color.MEDIUM_VIOLET_RED.getR()).toBe(199);
      expect(Color.MEDIUM_VIOLET_RED.getG()).toBe(21);
      expect(Color.MEDIUM_VIOLET_RED.getB()).toBe(133);
    });
  });
});
