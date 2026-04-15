import { describe, it, expect } from "vitest";
import { ColorUtil } from "../src/util/ColorUtil.js";
import { Color } from "../src/graphics/Color.js";

describe("LEGACY", () => {

describe("ColorUtil", () => {
  describe("toHex", () => {
    it("returns null for null input", () => {
      expect(ColorUtil.toHex(null)).toBeNull();
    });

    it("converts red to hex", () => {
      expect(ColorUtil.toHex(Color.RED)).toBe("ff0000");
    });

    it("converts green to hex", () => {
      expect(ColorUtil.toHex(Color.GREEN)).toBe("008000");
    });

    it("converts blue to hex", () => {
      expect(ColorUtil.toHex(Color.BLUE)).toBe("0000ff");
    });

    it("converts white to hex", () => {
      expect(ColorUtil.toHex(Color.WHITE)).toBe("ffffff");
    });

    it("converts black to hex", () => {
      expect(ColorUtil.toHex(Color.BLACK)).toBe("000000");
    });
  });

  describe("fromHexColor", () => {
    it("returns null for null input", () => {
      expect(ColorUtil.fromHexColor(null)).toBeNull();
    });

    it("parses 6-digit hex color", () => {
      expect(ColorUtil.fromHexColor("#0099CC")).toEqual(Color.fromRgb(0x00, 0x99, 0xcc));
    });

    it("parses 3-digit hex color", () => {
      expect(ColorUtil.fromHexColor("#09C")).toEqual(Color.fromRgb(0x00, 0x99, 0xcc));
    });

    it("trims whitespace", () => {
      expect(ColorUtil.fromHexColor("  #ff0000  ")).toEqual(Color.RED);
    });

    it("throws on invalid format", () => {
      expect(() => ColorUtil.fromHexColor("ff0000")).toThrow();
    });

    it("throws on invalid characters", () => {
      expect(() => ColorUtil.fromHexColor("#GGGGGG")).toThrow();
    });
  });

  describe("fromHex", () => {
    it("parses 6-digit hex triplet", () => {
      expect(ColorUtil.fromHex("0099CC")).toEqual(Color.fromRgb(0x00, 0x99, 0xcc));
    });

    it("parses 3-digit hex triplet", () => {
      expect(ColorUtil.fromHex("09C")).toEqual(Color.fromRgb(0x00, 0x99, 0xcc));
    });

    it("expands 3-digit correctly (F -> FF)", () => {
      expect(ColorUtil.fromHex("FFF")).toEqual(Color.WHITE);
    });

    it("expands 3-digit correctly (0 -> 00)", () => {
      expect(ColorUtil.fromHex("000")).toEqual(Color.BLACK);
    });

    it("trims whitespace", () => {
      expect(ColorUtil.fromHex("  ff0000  ")).toEqual(Color.RED);
    });

    it("throws on invalid length", () => {
      expect(() => ColorUtil.fromHex("0099C")).toThrow();
    });

    it("throws on invalid characters", () => {
      expect(() => ColorUtil.fromHex("GGGGGG")).toThrow();
    });
  });
});
});
