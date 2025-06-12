package dev.robocode.tankroyale.botapi.graphics;

/**
 * Represents an RGBA (red, green, blue, alpha) color for use in the Tank Royale game.
 * This class provides methods for creating and manipulating colors.
 */
public final class Color {
    private final int value;

    private Color(int value) {
        this.value = value;
    }

    // RGBA properties
    /**
     * Gets the red component value of this color.
     *
     * @return The red component value between 0 and 255.
     */
    public int getR() {
        return (value >> 24) & 0xFF;
    }

    /**
     * Gets the green component value of this color.
     *
     * @return The green component value between 0 and 255.
     */
    public int getG() {
        return (value >> 16) & 0xFF;
    }

    /**
     * Gets the blue component value of this color.
     *
     * @return The blue component value between 0 and 255.
     */
    public int getB() {
        return (value >> 8) & 0xFF;
    }

    /**
     * Gets the alpha component value of this color.
     *
     * @return The alpha component value between 0 and 255.
     */
    public int getA() {
        return value & 0xFF;
    }

    // Factory methods
    /**
     * Creates a color from a 32-bit RGBA value.
     *
     * @param rgba A 32-bit value specifying the RGBA components.
     * @return A new Color object initialized with the specified RGBA value.
     */
    public static Color fromRgba(int rgba) {
        return new Color(rgba);
    }

    /**
     * Creates a color from the specified red, green, blue, and alpha values.
     *
     * @param r The red component value (0-255).
     * @param g The green component value (0-255).
     * @param b The blue component value (0-255).
     * @param a The alpha component value (0-255).
     * @return A new Color object initialized with the specified RGBA values.
     */
    public static Color fromRgba(int r, int g, int b, int a) {
        return new Color((r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF));
    }

    /**
     * Creates a color from the specified red, green, and blue values, with an alpha value of 255 (fully opaque).
     *
     * @param r The red component value (0-255).
     * @param g The green component value (0-255).
     * @param b The blue component value (0-255).
     * @return A new Color object initialized with the specified RGB values and an alpha value of 255.
     */
    public static Color fromRgb(int r, int g, int b) {
        return fromRgba(r, g, b, 255);
    }

    /**
     * Creates a color from the specified base color with a new alpha value.
     *
     * @param baseColor The Color object from which to derive the RGB values.
     * @param a The alpha component value (0-255).
     * @return A new Color object with the RGB values from the base color and the specified alpha value.
     */
    public static Color fromRgba(Color baseColor, int a) {
        return fromRgba(baseColor.getR(), baseColor.getG(), baseColor.getB(), a);
    }

    /**
     * Converts this Color object to a 32-bit RGBA value.
     *
     * @return A 32-bit integer containing the RGBA representation of this color.
     */
    public int toRgba() {
        return value;
    }

    // Common colors
    public static final Color TRANSPARENT = fromRgba(255, 255, 255, 0);
    public static final Color ALICE_BLUE = fromRgb(240, 248, 255);
    public static final Color ANTIQUE_WHITE = fromRgb(250, 235, 215);
    public static final Color AQUA = fromRgb(0, 255, 255);
    public static final Color AQUAMARINE = fromRgb(127, 255, 212);
    public static final Color AZURE = fromRgb(240, 255, 255);
    public static final Color BEIGE = fromRgb(245, 245, 220);
    public static final Color BISQUE = fromRgb(255, 228, 196);
    public static final Color BLACK = fromRgb(0, 0, 0);
    public static final Color BLANCHED_ALMOND = fromRgb(255, 235, 205);
    public static final Color BLUE = fromRgb(0, 0, 255);
    public static final Color BLUE_VIOLET = fromRgb(138, 43, 226);
    public static final Color BROWN = fromRgb(165, 42, 42);
    public static final Color BURLY_WOOD = fromRgb(222, 184, 135);
    public static final Color CADET_BLUE = fromRgb(95, 158, 160);
    public static final Color CHARTREUSE = fromRgb(127, 255, 0);
    public static final Color CHOCOLATE = fromRgb(210, 105, 30);
    public static final Color CORAL = fromRgb(255, 127, 80);
    public static final Color CORNFLOWER_BLUE = fromRgb(100, 149, 237);
    public static final Color CORNSILK = fromRgb(255, 248, 220);
    public static final Color CRIMSON = fromRgb(220, 20, 60);
    public static final Color CYAN = fromRgb(0, 255, 255);
    public static final Color DARK_BLUE = fromRgb(0, 0, 139);
    public static final Color DARK_CYAN = fromRgb(0, 139, 139);
    public static final Color DARK_GOLDENROD = fromRgb(184, 134, 11);
    public static final Color DARK_GRAY = fromRgb(169, 169, 169);
    public static final Color DARK_GREEN = fromRgb(0, 100, 0);
    public static final Color DARK_KHAKI = fromRgb(189, 183, 107);
    public static final Color DARK_MAGENTA = fromRgb(139, 0, 139);
    public static final Color DARK_OLIVE_GREEN = fromRgb(85, 107, 47);
    public static final Color DARK_ORANGE = fromRgb(255, 140, 0);
    public static final Color DARK_ORCHID = fromRgb(153, 50, 204);
    public static final Color DARK_RED = fromRgb(139, 0, 0);
    public static final Color DARK_SALMON = fromRgb(233, 150, 122);
    public static final Color DARK_SEA_GREEN = fromRgb(143, 188, 139);
    public static final Color DARK_SLATE_BLUE = fromRgb(72, 61, 139);
    public static final Color DARK_SLATE_GRAY = fromRgb(47, 79, 79);
    public static final Color DARK_TURQUOISE = fromRgb(0, 206, 209);
    public static final Color DARK_VIOLET = fromRgb(148, 0, 211);
    public static final Color DEEP_PINK = fromRgb(255, 20, 147);
    public static final Color DEEP_SKY_BLUE = fromRgb(0, 191, 255);
    public static final Color DIM_GRAY = fromRgb(105, 105, 105);
    public static final Color DODGER_BLUE = fromRgb(30, 144, 255);
    public static final Color FIREBRICK = fromRgb(178, 34, 34);
    public static final Color FLORAL_WHITE = fromRgb(255, 250, 240);
    public static final Color FOREST_GREEN = fromRgb(34, 139, 34);
    public static final Color FUCHSIA = fromRgb(255, 0, 255);
    public static final Color GAINSBORO = fromRgb(220, 220, 220);
    public static final Color GHOST_WHITE = fromRgb(248, 248, 255);
    public static final Color GOLD = fromRgb(255, 215, 0);
    public static final Color GOLDENROD = fromRgb(218, 165, 32);
    public static final Color GRAY = fromRgb(128, 128, 128);
    public static final Color GREEN = fromRgb(0, 128, 0);
    public static final Color GREEN_YELLOW = fromRgb(173, 255, 47);
    public static final Color HONEYDEW = fromRgb(240, 255, 240);
    public static final Color HOT_PINK = fromRgb(255, 105, 180);
    public static final Color INDIAN_RED = fromRgb(205, 92, 92);
    public static final Color INDIGO = fromRgb(75, 0, 130);
    public static final Color IVORY = fromRgb(255, 255, 240);
    public static final Color KHAKI = fromRgb(240, 230, 140);
    public static final Color LAVENDER = fromRgb(230, 230, 250);
    public static final Color LAVENDER_BLUSH = fromRgb(255, 240, 245);
    public static final Color LAWN_GREEN = fromRgb(124, 252, 0);
    public static final Color LEMON_CHIFFON = fromRgb(255, 250, 205);
    public static final Color LIGHT_BLUE = fromRgb(173, 216, 230);
    public static final Color LIGHT_CORAL = fromRgb(240, 128, 128);
    public static final Color LIGHT_CYAN = fromRgb(224, 255, 255);
    public static final Color LIGHT_GOLDENROD_YELLOW = fromRgb(250, 250, 210);
    public static final Color LIGHT_GRAY = fromRgb(211, 211, 211);
    public static final Color LIGHT_GREEN = fromRgb(144, 238, 144);
    public static final Color LIGHT_PINK = fromRgb(255, 182, 193);
    public static final Color LIGHT_SALMON = fromRgb(255, 160, 122);
    public static final Color LIGHT_SEA_GREEN = fromRgb(32, 178, 170);
    public static final Color LIGHT_SKY_BLUE = fromRgb(135, 206, 250);
    public static final Color LIGHT_SLATE_GRAY = fromRgb(119, 136, 153);
    public static final Color LIGHT_STEEL_BLUE = fromRgb(176, 196, 222);
    public static final Color LIGHT_YELLOW = fromRgb(255, 255, 224);
    public static final Color LIME = fromRgb(0, 255, 0);
    public static final Color LIME_GREEN = fromRgb(50, 205, 50);
    public static final Color LINEN = fromRgb(250, 240, 230);
    public static final Color MAGENTA = fromRgb(255, 0, 255);
    public static final Color MAROON = fromRgb(128, 0, 0);
    public static final Color MEDIUM_AQUAMARINE = fromRgb(102, 205, 170);
    public static final Color MEDIUM_BLUE = fromRgb(0, 0, 205);
    public static final Color MEDIUM_ORCHID = fromRgb(186, 85, 211);
    public static final Color MEDIUM_PURPLE = fromRgb(147, 112, 219);
    public static final Color MEDIUM_SEA_GREEN = fromRgb(60, 179, 113);
    public static final Color MEDIUM_SLATE_BLUE = fromRgb(123, 104, 238);
    public static final Color MEDIUM_SPRING_GREEN = fromRgb(0, 250, 154);
    public static final Color MEDIUM_TURQUOISE = fromRgb(72, 209, 204);
    public static final Color MEDIUM_VIOLET_RED = fromRgb(199, 21, 133);
    public static final Color MIDNIGHT_BLUE = fromRgb(25, 25, 112);
    public static final Color MINT_CREAM = fromRgb(245, 255, 250);
    public static final Color MISTY_ROSE = fromRgb(255, 228, 225);
    public static final Color MOCCASIN = fromRgb(255, 228, 181);
    public static final Color NAVAJO_WHITE = fromRgb(255, 222, 173);
    public static final Color NAVY = fromRgb(0, 0, 128);
    public static final Color OLD_LACE = fromRgb(253, 245, 230);
    public static final Color OLIVE = fromRgb(128, 128, 0);
    public static final Color OLIVE_DRAB = fromRgb(107, 142, 35);
    public static final Color ORANGE = fromRgb(255, 165, 0);
    public static final Color ORANGE_RED = fromRgb(255, 69, 0);
    public static final Color ORCHID = fromRgb(218, 112, 214);
    public static final Color PALE_GOLDENROD = fromRgb(238, 232, 170);
    public static final Color PALE_GREEN = fromRgb(152, 251, 152);
    public static final Color PALE_TURQUOISE = fromRgb(175, 238, 238);
    public static final Color PALE_VIOLET_RED = fromRgb(219, 112, 147);
    public static final Color PAPAYA_WHIP = fromRgb(255, 239, 213);
    public static final Color PEACH_PUFF = fromRgb(255, 218, 185);
    public static final Color PERU = fromRgb(205, 133, 63);
    public static final Color PINK = fromRgb(255, 192, 203);
    public static final Color PLUM = fromRgb(221, 160, 221);
    public static final Color POWDER_BLUE = fromRgb(176, 224, 230);
    public static final Color PURPLE = fromRgb(128, 0, 128);
    public static final Color RED = fromRgb(255, 0, 0);
    public static final Color ROSY_BROWN = fromRgb(188, 143, 143);
    public static final Color ROYAL_BLUE = fromRgb(65, 105, 225);
    public static final Color SADDLE_BROWN = fromRgb(139, 69, 19);
    public static final Color SALMON = fromRgb(250, 128, 114);
    public static final Color SANDY_BROWN = fromRgb(244, 164, 96);
    public static final Color SEA_GREEN = fromRgb(46, 139, 87);
    public static final Color SEA_SHELL = fromRgb(255, 245, 238);
    public static final Color SIENNA = fromRgb(160, 82, 45);
    public static final Color SILVER = fromRgb(192, 192, 192);
    public static final Color SKY_BLUE = fromRgb(135, 206, 235);
    public static final Color SLATE_BLUE = fromRgb(106, 90, 205);
    public static final Color SLATE_GRAY = fromRgb(112, 128, 144);
    public static final Color SNOW = fromRgb(255, 250, 250);
    public static final Color SPRING_GREEN = fromRgb(0, 255, 127);
    public static final Color STEEL_BLUE = fromRgb(70, 130, 180);
    public static final Color TAN = fromRgb(210, 180, 140);
    public static final Color TEAL = fromRgb(0, 128, 128);
    public static final Color THISTLE = fromRgb(216, 191, 216);
    public static final Color TOMATO = fromRgb(255, 99, 71);
    public static final Color TURQUOISE = fromRgb(64, 224, 208);
    public static final Color VIOLET = fromRgb(238, 130, 238);
    public static final Color WHEAT = fromRgb(245, 222, 179);
    public static final Color WHITE = fromRgb(255, 255, 255);
    public static final Color WHITE_SMOKE = fromRgb(245, 245, 245);
    public static final Color YELLOW = fromRgb(255, 255, 0);
    public static final Color YELLOW_GREEN = fromRgb(154, 205, 50);

    // Equality and comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Color other = (Color) obj;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    // String representation
    @Override
    public String toString() {
        if (getA() == 255) {
            return String.format("Color(r=%d, g=%d, b=%d)", getR(), getG(), getB());
        }
        return String.format("Color(r=%d, g=%d, b=%d, a=%d)", getR(), getG(), getB(), getA());
    }

    /**
     * Converts the color to its hexadecimal representation.
     *
     * @return A string representing the color in hexadecimal format:
     *         - If alpha is 255 (fully opaque), returns #RRGGBB
     *         - If alpha is not 255, returns #RRGGBBAA
     */
    public String toHexColor() {
        if (getA() == 255) {
            return String.format("#%02X%02X%02X", getR(), getG(), getB());
        }
        return String.format("#%02X%02X%02X%02X", getR(), getG(), getB(), getA());
    }
}
