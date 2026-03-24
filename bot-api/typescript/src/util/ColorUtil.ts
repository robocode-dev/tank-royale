import { Color } from "../graphics/Color.js";

const NUMERIC_RGB = /^#[0-9a-fA-F]{3,6}$/;
const HEX_DIGITS = /^(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/;

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
    throw new Error('You must supply the string in numeric RGB format #[0-9a-fA-F], e.g. "#09C" or "#0099CC"');
  },

  fromHex(hexTriplet: string): Color {
    hexTriplet = hexTriplet.trim();
    if (!HEX_DIGITS.test(hexTriplet)) {
      throw new Error('You must supply 3 or 6 hex digits [0-9a-fA-F], e.g. "09C" or "0099CC"');
    }
    const isThreeDigits = hexTriplet.length === 3;
    const len = isThreeDigits ? 1 : 2;
    let r = parseInt(hexTriplet.substring(0, len), 16);
    let g = parseInt(hexTriplet.substring(len, len * 2), 16);
    let b = parseInt(hexTriplet.substring(len * 2, len * 3), 16);
    if (isThreeDigits) {
      r = (r << 4) | r;
      g = (g << 4) | g;
      b = (b << 4) | b;
    }
    return Color.fromRgb(r, g, b);
  },
};

function toHexByte(value: number): string {
  return ((value >> 4) & 0xf).toString(16) + (value & 0xf).toString(16);
}
