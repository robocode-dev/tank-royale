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
    public static Color fromRgba(int r, int g, int b) {
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
    public static final Color ALICE_BLUE = fromRgba(240, 248, 255);
    public static final Color ANTIQUE_WHITE = fromRgba(250, 235, 215);
    public static final Color AQUA = fromRgba(0, 255, 255);
    public static final Color AQUAMARINE = fromRgba(127, 255, 212);
    public static final Color AZURE = fromRgba(240, 255, 255);
    public static final Color BEIGE = fromRgba(245, 245, 220);
    public static final Color BISQUE = fromRgba(255, 228, 196);
    public static final Color BLACK = fromRgba(0, 0, 0);
    public static final Color BLANCHED_ALMOND = fromRgba(255, 235, 205);
    public static final Color BLUE = fromRgba(0, 0, 255);
    public static final Color BLUE_VIOLET = fromRgba(138, 43, 226);
    public static final Color BROWN = fromRgba(165, 42, 42);
    public static final Color BURLY_WOOD = fromRgba(222, 184, 135);
    public static final Color CADET_BLUE = fromRgba(95, 158, 160);
    public static final Color CHARTREUSE = fromRgba(127, 255, 0);
    public static final Color CHOCOLATE = fromRgba(210, 105, 30);
    public static final Color CORAL = fromRgba(255, 127, 80);
    public static final Color CORNFLOWER_BLUE = fromRgba(100, 149, 237);
    public static final Color CORNSILK = fromRgba(255, 248, 220);
    public static final Color CRIMSON = fromRgba(220, 20, 60);
    public static final Color CYAN = fromRgba(0, 255, 255);
    public static final Color DARK_BLUE = fromRgba(0, 0, 139);
    public static final Color DARK_CYAN = fromRgba(0, 139, 139);
    public static final Color DARK_GOLDENROD = fromRgba(184, 134, 11);
    public static final Color DARK_GRAY = fromRgba(169, 169, 169);
    public static final Color DARK_GREEN = fromRgba(0, 100, 0);
    public static final Color DARK_KHAKI = fromRgba(189, 183, 107);
    public static final Color DARK_MAGENTA = fromRgba(139, 0, 139);
    public static final Color DARK_OLIVE_GREEN = fromRgba(85, 107, 47);
    public static final Color DARK_ORANGE = fromRgba(255, 140, 0);
    public static final Color DARK_ORCHID = fromRgba(153, 50, 204);
    public static final Color DARK_RED = fromRgba(139, 0, 0);
    public static final Color DARK_SALMON = fromRgba(233, 150, 122);
    public static final Color DARK_SEA_GREEN = fromRgba(143, 188, 139);
    public static final Color DARK_SLATE_BLUE = fromRgba(72, 61, 139);
    public static final Color DARK_SLATE_GRAY = fromRgba(47, 79, 79);
    public static final Color DARK_TURQUOISE = fromRgba(0, 206, 209);
    public static final Color DARK_VIOLET = fromRgba(148, 0, 211);
    public static final Color DEEP_PINK = fromRgba(255, 20, 147);
    public static final Color DEEP_SKY_BLUE = fromRgba(0, 191, 255);
    public static final Color DIM_GRAY = fromRgba(105, 105, 105);
    public static final Color DODGER_BLUE = fromRgba(30, 144, 255);
    public static final Color FIREBRICK = fromRgba(178, 34, 34);
    public static final Color FLORAL_WHITE = fromRgba(255, 250, 240);
    public static final Color FOREST_GREEN = fromRgba(34, 139, 34);
    public static final Color FUCHSIA = fromRgba(255, 0, 255);
    public static final Color GAINSBORO = fromRgba(220, 220, 220);
    public static final Color GHOST_WHITE = fromRgba(248, 248, 255);
    public static final Color GOLD = fromRgba(255, 215, 0);
    public static final Color GOLDENROD = fromRgba(218, 165, 32);
    public static final Color GRAY = fromRgba(128, 128, 128);
    public static final Color GREEN = fromRgba(0, 128, 0);
    public static final Color GREEN_YELLOW = fromRgba(173, 255, 47);
    public static final Color HONEYDEW = fromRgba(240, 255, 240);
    public static final Color HOT_PINK = fromRgba(255, 105, 180);
    public static final Color INDIAN_RED = fromRgba(205, 92, 92);
    public static final Color INDIGO = fromRgba(75, 0, 130);
    public static final Color IVORY = fromRgba(255, 255, 240);
    public static final Color KHAKI = fromRgba(240, 230, 140);
    public static final Color LAVENDER = fromRgba(230, 230, 250);
    public static final Color LAVENDER_BLUSH = fromRgba(255, 240, 245);
    public static final Color LAWN_GREEN = fromRgba(124, 252, 0);
    public static final Color LEMON_CHIFFON = fromRgba(255, 250, 205);
    public static final Color LIGHT_BLUE = fromRgba(173, 216, 230);
    public static final Color LIGHT_CORAL = fromRgba(240, 128, 128);
    public static final Color LIGHT_CYAN = fromRgba(224, 255, 255);
    public static final Color LIGHT_GOLDENROD_YELLOW = fromRgba(250, 250, 210);
    public static final Color LIGHT_GRAY = fromRgba(211, 211, 211);
    public static final Color LIGHT_GREEN = fromRgba(144, 238, 144);
    public static final Color LIGHT_PINK = fromRgba(255, 182, 193);
    public static final Color LIGHT_SALMON = fromRgba(255, 160, 122);
    public static final Color LIGHT_SEA_GREEN = fromRgba(32, 178, 170);
    public static final Color LIGHT_SKY_BLUE = fromRgba(135, 206, 250);
    public static final Color LIGHT_SLATE_GRAY = fromRgba(119, 136, 153);
    public static final Color LIGHT_STEEL_BLUE = fromRgba(176, 196, 222);
    public static final Color LIGHT_YELLOW = fromRgba(255, 255, 224);
    public static final Color LIME = fromRgba(0, 255, 0);
    public static final Color LIME_GREEN = fromRgba(50, 205, 50);
    public static final Color LINEN = fromRgba(250, 240, 230);
    public static final Color MAGENTA = fromRgba(255, 0, 255);
    public static final Color MAROON = fromRgba(128, 0, 0);
    public static final Color MEDIUM_AQUAMARINE = fromRgba(102, 205, 170);
    public static final Color MEDIUM_BLUE = fromRgba(0, 0, 205);
    public static final Color MEDIUM_ORCHID = fromRgba(186, 85, 211);
    public static final Color MEDIUM_PURPLE = fromRgba(147, 112, 219);
    public static final Color MEDIUM_SEA_GREEN = fromRgba(60, 179, 113);
    public static final Color MEDIUM_SLATE_BLUE = fromRgba(123, 104, 238);
    public static final Color MEDIUM_SPRING_GREEN = fromRgba(0, 250, 154);
    public static final Color MEDIUM_TURQUOISE = fromRgba(72, 209, 204);
    public static final Color MEDIUM_VIOLET_RED = fromRgba(199, 21, 133);
    public static final Color MIDNIGHT_BLUE = fromRgba(25, 25, 112);
    public static final Color MINT_CREAM = fromRgba(245, 255, 250);
    public static final Color MISTY_ROSE = fromRgba(255, 228, 225);
    public static final Color MOCCASIN = fromRgba(255, 228, 181);
    public static final Color NAVAJO_WHITE = fromRgba(255, 222, 173);
    public static final Color NAVY = fromRgba(0, 0, 128);
    public static final Color OLD_LACE = fromRgba(253, 245, 230);
    public static final Color OLIVE = fromRgba(128, 128, 0);
    public static final Color OLIVE_DRAB = fromRgba(107, 142, 35);
    public static final Color ORANGE = fromRgba(255, 165, 0);
    public static final Color ORANGE_RED = fromRgba(255, 69, 0);
    public static final Color ORCHID = fromRgba(218, 112, 214);
    public static final Color PALE_GOLDENROD = fromRgba(238, 232, 170);
    public static final Color PALE_GREEN = fromRgba(152, 251, 152);
    public static final Color PALE_TURQUOISE = fromRgba(175, 238, 238);
    public static final Color PALE_VIOLET_RED = fromRgba(219, 112, 147);
    public static final Color PAPAYA_WHIP = fromRgba(255, 239, 213);
    public static final Color PEACH_PUFF = fromRgba(255, 218, 185);
    public static final Color PERU = fromRgba(205, 133, 63);
    public static final Color PINK = fromRgba(255, 192, 203);
    public static final Color PLUM = fromRgba(221, 160, 221);
    public static final Color POWDER_BLUE = fromRgba(176, 224, 230);
    public static final Color PURPLE = fromRgba(128, 0, 128);
    public static final Color RED = fromRgba(255, 0, 0);
    public static final Color ROSY_BROWN = fromRgba(188, 143, 143);
    public static final Color ROYAL_BLUE = fromRgba(65, 105, 225);
    public static final Color SADDLE_BROWN = fromRgba(139, 69, 19);
    public static final Color SALMON = fromRgba(250, 128, 114);
    public static final Color SANDY_BROWN = fromRgba(244, 164, 96);
    public static final Color SEA_GREEN = fromRgba(46, 139, 87);
    public static final Color SEA_SHELL = fromRgba(255, 245, 238);
    public static final Color SIENNA = fromRgba(160, 82, 45);
    public static final Color SILVER = fromRgba(192, 192, 192);
    public static final Color SKY_BLUE = fromRgba(135, 206, 235);
    public static final Color SLATE_BLUE = fromRgba(106, 90, 205);
    public static final Color SLATE_GRAY = fromRgba(112, 128, 144);
    public static final Color SNOW = fromRgba(255, 250, 250);
    public static final Color SPRING_GREEN = fromRgba(0, 255, 127);
    public static final Color STEEL_BLUE = fromRgba(70, 130, 180);
    public static final Color TAN = fromRgba(210, 180, 140);
    public static final Color TEAL = fromRgba(0, 128, 128);
    public static final Color THISTLE = fromRgba(216, 191, 216);
    public static final Color TOMATO = fromRgba(255, 99, 71);
    public static final Color TURQUOISE = fromRgba(64, 224, 208);
    public static final Color VIOLET = fromRgba(238, 130, 238);
    public static final Color WHEAT = fromRgba(245, 222, 179);
    public static final Color WHITE = fromRgba(255, 255, 255);
    public static final Color WHITE_SMOKE = fromRgba(245, 245, 245);
    public static final Color YELLOW = fromRgba(255, 255, 0);
    public static final Color YELLOW_GREEN = fromRgba(154, 205, 50);

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
