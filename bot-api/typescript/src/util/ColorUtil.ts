import { Color } from "../graphics/Color.js";

// 3 (RGB), 4 (RGBA), 6 (RRGGBB) or 8 (RRGGBBAA) hex digits, in capital or small letters
const NUMERIC_RGB = /^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/;
const HEX_DIGITS = /^(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/;

export const ColorUtil = {
  toHex(color: Color | null): string | null {
    if (color === null) return null;
    return toHexByte(color.getR()) + toHexByte(color.getG()) + toHexByte(color.getB());
  },

  fromHexColor(str: string | null): Color | null {
    if (str === null) return null;
    str = str.trim();
    if (NUMERIC_RGB.test(str)) {
      return ColorUtil.fromHex(str.substring(1));
    }
    throw new Error('You must supply the string in the format #RGB, #RGBA, #RRGGBB or #RRGGBBAA, e.g. "#09C" or "#0099CC"');
  },

  /**
   * Creates a color from a hex string without a leading hash sign: 3 digits (RGB), 4 digits (RGBA), 6 digits
   * (RRGGBB) or 8 digits (RRGGBBAA), in capital or small letters. In the short forms each digit is doubled.
   * Without alpha digits the color is fully opaque.
   */
  fromHex(hexTriplet: string): Color {
    hexTriplet = hexTriplet.trim();
    if (!HEX_DIGITS.test(hexTriplet)) {
      throw new Error('You must supply 3, 4, 6 or 8 hex digits [0-9a-fA-F], e.g. "09C" or "0099CC"');
    }
    const isShort = hexTriplet.length === 3 || hexTriplet.length === 4;
    const hasAlpha = hexTriplet.length === 4 || hexTriplet.length === 8;
    const len = isShort ? 1 : 2;
    let r = parseInt(hexTriplet.substring(0, len), 16);
    let g = parseInt(hexTriplet.substring(len, len * 2), 16);
    let b = parseInt(hexTriplet.substring(len * 2, len * 3), 16);
    let a = hasAlpha ? parseInt(hexTriplet.substring(len * 3, len * 4), 16) : 255;
    if (isShort) {
      r = (r << 4) | r;
      g = (g << 4) | g;
      b = (b << 4) | b;
      if (hasAlpha) a = (a << 4) | a;
    }
    return Color.fromRgba(r, g, b, a);
  },
};

function toHexByte(value: number): string {
  return ((value >> 4) & 0xf).toString(16) + (value & 0xf).toString(16);
}
