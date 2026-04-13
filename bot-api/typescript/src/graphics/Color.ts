/**
 * Represents an RGBA (red, green, blue, alpha) color for use in the Tank Royale game.
 */
export class Color {
  private readonly value: number;

  private constructor(value: number) {
    this.value = value >>> 0; // ensure unsigned 32-bit
  }

  getR(): number {
    return (this.value >>> 24) & 0xff;
  }

  getG(): number {
    return (this.value >>> 16) & 0xff;
  }

  getB(): number {
    return (this.value >>> 8) & 0xff;
  }

  getA(): number {
    return this.value & 0xff;
  }

  toRgba(): number {
    return this.value;
  }

  toHexColor(): string {
    const r = this.getR().toString(16).padStart(2, "0").toUpperCase();
    const g = this.getG().toString(16).padStart(2, "0").toUpperCase();
    const b = this.getB().toString(16).padStart(2, "0").toUpperCase();
    if (this.getA() === 255) {
      return `#${r}${g}${b}`;
    }
    const a = this.getA().toString(16).padStart(2, "0").toUpperCase();
    return `#${r}${g}${b}${a}`;
  }

  equals(other: unknown): boolean {
    if (this === other) return true;
    if (!(other instanceof Color)) return false;
    return this.value === other.value;
  }

  toString(): string {
    if (this.getA() === 255) {
      return `Color(r=${this.getR()}, g=${this.getG()}, b=${this.getB()})`;
    }
    return `Color(r=${this.getR()}, g=${this.getG()}, b=${this.getB()}, a=${this.getA()})`;
  }

  static fromRgba(rgba: number): Color;
  static fromRgba(r: number, g: number, b: number, a: number): Color;
  static fromRgba(baseColor: Color, a: number): Color;
  static fromRgba(
    rOrRgbaOrBase: number | Color,
    g?: number,
    b?: number,
    a?: number
  ): Color {
    if (rOrRgbaOrBase instanceof Color) {
      const base = rOrRgbaOrBase;
      return Color.fromRgba(base.getR(), base.getG(), base.getB(), g!);
    }
    if (g === undefined) {
      return new Color(rOrRgbaOrBase >>> 0);
    }
    const r = rOrRgbaOrBase;
    return new Color(
      (((r & 0xff) << 24) | ((g & 0xff) << 16) | ((b! & 0xff) << 8) | (a! & 0xff)) >>> 0
    );
  }

  static fromRgb(r: number, g: number, b: number): Color {
    return Color.fromRgba(r, g, b, 255);
  }

  // Named color constants
  static readonly TRANSPARENT = Color.fromRgba(255, 255, 255, 0);
  static readonly ALICE_BLUE = Color.fromRgb(240, 248, 255);
  static readonly ANTIQUE_WHITE = Color.fromRgb(250, 235, 215);
  static readonly AQUA = Color.fromRgb(0, 255, 255);
  static readonly AQUAMARINE = Color.fromRgb(127, 255, 212);
  static readonly AZURE = Color.fromRgb(240, 255, 255);
  static readonly BEIGE = Color.fromRgb(245, 245, 220);
  static readonly BISQUE = Color.fromRgb(255, 228, 196);
  static readonly BLACK = Color.fromRgb(0, 0, 0);
  static readonly BLANCHED_ALMOND = Color.fromRgb(255, 235, 205);
  static readonly BLUE = Color.fromRgb(0, 0, 255);
  static readonly BLUE_VIOLET = Color.fromRgb(138, 43, 226);
  static readonly BROWN = Color.fromRgb(165, 42, 42);
  static readonly BURLY_WOOD = Color.fromRgb(222, 184, 135);
  static readonly CADET_BLUE = Color.fromRgb(95, 158, 160);
  static readonly CHARTREUSE = Color.fromRgb(127, 255, 0);
  static readonly CHOCOLATE = Color.fromRgb(210, 105, 30);
  static readonly CORAL = Color.fromRgb(255, 127, 80);
  static readonly CORNFLOWER_BLUE = Color.fromRgb(100, 149, 237);
  static readonly CORNSILK = Color.fromRgb(255, 248, 220);
  static readonly CRIMSON = Color.fromRgb(220, 20, 60);
  static readonly CYAN = Color.fromRgb(0, 255, 255);
  static readonly DARK_BLUE = Color.fromRgb(0, 0, 139);
  static readonly DARK_CYAN = Color.fromRgb(0, 139, 139);
  static readonly DARK_GOLDENROD = Color.fromRgb(184, 134, 11);
  static readonly DARK_GRAY = Color.fromRgb(169, 169, 169);
  static readonly DARK_GREEN = Color.fromRgb(0, 100, 0);
  static readonly DARK_KHAKI = Color.fromRgb(189, 183, 107);
  static readonly DARK_MAGENTA = Color.fromRgb(139, 0, 139);
  static readonly DARK_OLIVE_GREEN = Color.fromRgb(85, 107, 47);
  static readonly DARK_ORANGE = Color.fromRgb(255, 140, 0);
  static readonly DARK_ORCHID = Color.fromRgb(153, 50, 204);
  static readonly DARK_RED = Color.fromRgb(139, 0, 0);
  static readonly DARK_SALMON = Color.fromRgb(233, 150, 122);
  static readonly DARK_SEA_GREEN = Color.fromRgb(143, 188, 139);
  static readonly DARK_SLATE_BLUE = Color.fromRgb(72, 61, 139);
  static readonly DARK_SLATE_GRAY = Color.fromRgb(47, 79, 79);
  static readonly DARK_TURQUOISE = Color.fromRgb(0, 206, 209);
  static readonly DARK_VIOLET = Color.fromRgb(148, 0, 211);
  static readonly DEEP_PINK = Color.fromRgb(255, 20, 147);
  static readonly DEEP_SKY_BLUE = Color.fromRgb(0, 191, 255);
  static readonly DIM_GRAY = Color.fromRgb(105, 105, 105);
  static readonly DODGER_BLUE = Color.fromRgb(30, 144, 255);
  static readonly FIREBRICK = Color.fromRgb(178, 34, 34);
  static readonly FLORAL_WHITE = Color.fromRgb(255, 250, 240);
  static readonly FOREST_GREEN = Color.fromRgb(34, 139, 34);
  static readonly FUCHSIA = Color.fromRgb(255, 0, 255);
  static readonly GAINSBORO = Color.fromRgb(220, 220, 220);
  static readonly GHOST_WHITE = Color.fromRgb(248, 248, 255);
  static readonly GOLD = Color.fromRgb(255, 215, 0);
  static readonly GOLDENROD = Color.fromRgb(218, 165, 32);
  static readonly GRAY = Color.fromRgb(128, 128, 128);
  static readonly GREEN = Color.fromRgb(0, 128, 0);
  static readonly GREEN_YELLOW = Color.fromRgb(173, 255, 47);
  static readonly HONEYDEW = Color.fromRgb(240, 255, 240);
  static readonly HOT_PINK = Color.fromRgb(255, 105, 180);
  static readonly INDIAN_RED = Color.fromRgb(205, 92, 92);
  static readonly INDIGO = Color.fromRgb(75, 0, 130);
  static readonly IVORY = Color.fromRgb(255, 255, 240);
  static readonly KHAKI = Color.fromRgb(240, 230, 140);
  static readonly LAVENDER = Color.fromRgb(230, 230, 250);
  static readonly LAVENDER_BLUSH = Color.fromRgb(255, 240, 245);
  static readonly LAWN_GREEN = Color.fromRgb(124, 252, 0);
  static readonly LEMON_CHIFFON = Color.fromRgb(255, 250, 205);
  static readonly LIGHT_BLUE = Color.fromRgb(173, 216, 230);
  static readonly LIGHT_CORAL = Color.fromRgb(240, 128, 128);
  static readonly LIGHT_CYAN = Color.fromRgb(224, 255, 255);
  static readonly LIGHT_GOLDENROD_YELLOW = Color.fromRgb(250, 250, 210);
  static readonly LIGHT_GRAY = Color.fromRgb(211, 211, 211);
  static readonly LIGHT_GREEN = Color.fromRgb(144, 238, 144);
  static readonly LIGHT_PINK = Color.fromRgb(255, 182, 193);
  static readonly LIGHT_SALMON = Color.fromRgb(255, 160, 122);
  static readonly LIGHT_SEA_GREEN = Color.fromRgb(32, 178, 170);
  static readonly LIGHT_SKY_BLUE = Color.fromRgb(135, 206, 250);
  static readonly LIGHT_SLATE_GRAY = Color.fromRgb(119, 136, 153);
  static readonly LIGHT_STEEL_BLUE = Color.fromRgb(176, 196, 222);
  static readonly LIGHT_YELLOW = Color.fromRgb(255, 255, 224);
  static readonly LIME = Color.fromRgb(0, 255, 0);
  static readonly LIME_GREEN = Color.fromRgb(50, 205, 50);
  static readonly LINEN = Color.fromRgb(250, 240, 230);
  static readonly MAGENTA = Color.fromRgb(255, 0, 255);
  static readonly MAROON = Color.fromRgb(128, 0, 0);
  static readonly MEDIUM_AQUAMARINE = Color.fromRgb(102, 205, 170);
  static readonly MEDIUM_BLUE = Color.fromRgb(0, 0, 205);
  static readonly MEDIUM_ORCHID = Color.fromRgb(186, 85, 211);
  static readonly MEDIUM_PURPLE = Color.fromRgb(147, 112, 219);
  static readonly MEDIUM_SEA_GREEN = Color.fromRgb(60, 179, 113);
  static readonly MEDIUM_SLATE_BLUE = Color.fromRgb(123, 104, 238);
  static readonly MEDIUM_SPRING_GREEN = Color.fromRgb(0, 250, 154);
  static readonly MEDIUM_TURQUOISE = Color.fromRgb(72, 209, 204);
  static readonly MEDIUM_VIOLET_RED = Color.fromRgb(199, 21, 133);
  static readonly MIDNIGHT_BLUE = Color.fromRgb(25, 25, 112);
  static readonly MINT_CREAM = Color.fromRgb(245, 255, 250);
  static readonly MISTY_ROSE = Color.fromRgb(255, 228, 225);
  static readonly MOCCASIN = Color.fromRgb(255, 228, 181);
  static readonly NAVAJO_WHITE = Color.fromRgb(255, 222, 173);
  static readonly NAVY = Color.fromRgb(0, 0, 128);
  static readonly OLD_LACE = Color.fromRgb(253, 245, 230);
  static readonly OLIVE = Color.fromRgb(128, 128, 0);
  static readonly OLIVE_DRAB = Color.fromRgb(107, 142, 35);
  static readonly ORANGE = Color.fromRgb(255, 165, 0);
  static readonly ORANGE_RED = Color.fromRgb(255, 69, 0);
  static readonly ORCHID = Color.fromRgb(218, 112, 214);
  static readonly PALE_GOLDENROD = Color.fromRgb(238, 232, 170);
  static readonly PALE_GREEN = Color.fromRgb(152, 251, 152);
  static readonly PALE_TURQUOISE = Color.fromRgb(175, 238, 238);
  static readonly PALE_VIOLET_RED = Color.fromRgb(219, 112, 147);
  static readonly PAPAYA_WHIP = Color.fromRgb(255, 239, 213);
  static readonly PEACH_PUFF = Color.fromRgb(255, 218, 185);
  static readonly PERU = Color.fromRgb(205, 133, 63);
  static readonly PINK = Color.fromRgb(255, 192, 203);
  static readonly PLUM = Color.fromRgb(221, 160, 221);
  static readonly POWDER_BLUE = Color.fromRgb(176, 224, 230);
  static readonly PURPLE = Color.fromRgb(128, 0, 128);
  static readonly RED = Color.fromRgb(255, 0, 0);
  static readonly ROSY_BROWN = Color.fromRgb(188, 143, 143);
  static readonly ROYAL_BLUE = Color.fromRgb(65, 105, 225);
  static readonly SADDLE_BROWN = Color.fromRgb(139, 69, 19);
  static readonly SALMON = Color.fromRgb(250, 128, 114);
  static readonly SANDY_BROWN = Color.fromRgb(244, 164, 96);
  static readonly SEA_GREEN = Color.fromRgb(46, 139, 87);
  static readonly SEA_SHELL = Color.fromRgb(255, 245, 238);
  static readonly SIENNA = Color.fromRgb(160, 82, 45);
  static readonly SILVER = Color.fromRgb(192, 192, 192);
  static readonly SKY_BLUE = Color.fromRgb(135, 206, 235);
  static readonly SLATE_BLUE = Color.fromRgb(106, 90, 205);
  static readonly SLATE_GRAY = Color.fromRgb(112, 128, 144);
  static readonly SNOW = Color.fromRgb(255, 250, 250);
  static readonly SPRING_GREEN = Color.fromRgb(0, 255, 127);
  static readonly STEEL_BLUE = Color.fromRgb(70, 130, 180);
  static readonly TAN = Color.fromRgb(210, 180, 140);
  static readonly TEAL = Color.fromRgb(0, 128, 128);
  static readonly THISTLE = Color.fromRgb(216, 191, 216);
  static readonly TOMATO = Color.fromRgb(255, 99, 71);
  static readonly TURQUOISE = Color.fromRgb(64, 224, 208);
  static readonly VIOLET = Color.fromRgb(238, 130, 238);
  static readonly WHEAT = Color.fromRgb(245, 222, 179);
  static readonly WHITE = Color.fromRgb(255, 255, 255);
  static readonly WHITE_SMOKE = Color.fromRgb(245, 245, 245);
  static readonly YELLOW = Color.fromRgb(255, 255, 0);
  static readonly YELLOW_GREEN = Color.fromRgb(154, 205, 50);
}
