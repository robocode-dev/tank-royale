package dev.robocode.tankroyale.botapi.graphics

import dev.robocode.tankroyale.utils.StringFormatUtil

/**
 * Represents an RGBA (red, green, blue, alpha) color for use in the Tank Royale game.
 * This class provides methods for creating and manipulating colors.
 */
class Color private constructor(private val value: Int) {

    // RGBA properties
    /**
     * The red component value between 0 and 255.
     */
    val r: Int
        get() = (value shr 24) and 0xFF

    /**
     * The green component value between 0 and 255.
     */
    val g: Int
        get() = (value shr 16) and 0xFF

    /**
     * The blue component value between 0 and 255.
     */
    val b: Int
        get() = (value shr 8) and 0xFF

    /**
     * The alpha component value between 0 and 255.
     */
    val a: Int
        get() = value and 0xFF

    /**
     * Returns this color as a 32-bit RGBA integer.
     */
    fun toRgba(): Int = value

    // Equality and comparison
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Color
        return value == other.value
    }

    override fun hashCode(): Int = value

    // String representation
    override fun toString(): String {
        return if (a == 255) {
            StringFormatUtil.formatRgbColor(r, g, b)
        } else {
            StringFormatUtil.formatRgbaColor(r, g, b, a)
        }
    }

    /**
     * Returns the color in hexadecimal notation.  
     * - `#RRGGBB` when alpha is 255 (fully opaque)  
     * - `#RRGGBBAA` otherwise
     */
    fun toHexColor(): String {
        return if (a == 255) {
            StringFormatUtil.formatHexRgb(r, g, b)
        } else {
            StringFormatUtil.formatHexRgba(r, g, b, a)
        }
    }

    companion object {
        // Factory methods
        /**
         * Creates a color from a 32-bit RGBA value.
         *
         * @param rgba A 32-bit value specifying the RGBA components.
         * @return A new Color object initialized with the specified RGBA value.
         */
        fun fromRgba(rgba: Int): Color = Color(rgba)

        /**
         * Creates a color from the specified red, green, blue, and alpha values.
         *
         * @param r The red component value (0-255).
         * @param g The green component value (0-255).
         * @param b The blue component value (0-255).
         * @param a The alpha component value (0-255).
         * @return A new Color object initialized with the specified RGBA values.
         */
        fun fromRgba(r: Int, g: Int, b: Int, a: Int): Color {
            return Color((r and 0xFF) shl 24 or ((g and 0xFF) shl 16) or ((b and 0xFF) shl 8) or (a and 0xFF))
        }

        /**
         * Creates a color from the specified red, green, and blue values, with an alpha value of 255 (fully opaque).
         *
         * @param r The red component value (0-255).
         * @param g The green component value (0-255).
         * @param b The blue component value (0-255).
         * @return A new Color object initialized with the specified RGB values and an alpha value of 255.
         */
        fun fromRgb(r: Int, g: Int, b: Int): Color = fromRgba(r, g, b, 255)

        /**
         * Creates a color from the specified base color with a new alpha value.
         *
         * @param baseColor The Color object from which to derive the RGB values.
         * @param a The alpha component value (0-255).
         * @return A new Color object with the RGB values from the base color and the specified alpha value.
         */
        fun fromRgba(baseColor: Color, a: Int): Color = fromRgba(baseColor.r, baseColor.g, baseColor.b, a)

        // Common colors
        val TRANSPARENT = fromRgba(255, 255, 255, 0)
        val ALICE_BLUE = fromRgb(240, 248, 255)
        val ANTIQUE_WHITE = fromRgb(250, 235, 215)
        val AQUA = fromRgb(0, 255, 255)
        val AQUAMARINE = fromRgb(127, 255, 212)
        val AZURE = fromRgb(240, 255, 255)
        val BEIGE = fromRgb(245, 245, 220)
        val BISQUE = fromRgb(255, 228, 196)
        val BLACK = fromRgb(0, 0, 0)
        val BLANCHED_ALMOND = fromRgb(255, 235, 205)
        val BLUE = fromRgb(0, 0, 255)
        val BLUE_VIOLET = fromRgb(138, 43, 226)
        val BROWN = fromRgb(165, 42, 42)
        val BURLY_WOOD = fromRgb(222, 184, 135)
        val CADET_BLUE = fromRgb(95, 158, 160)
        val CHARTREUSE = fromRgb(127, 255, 0)
        val CHOCOLATE = fromRgb(210, 105, 30)
        val CORAL = fromRgb(255, 127, 80)
        val CORNFLOWER_BLUE = fromRgb(100, 149, 237)
        val CORNSILK = fromRgb(255, 248, 220)
        val CRIMSON = fromRgb(220, 20, 60)
        val CYAN = fromRgb(0, 255, 255)
        val DARK_BLUE = fromRgb(0, 0, 139)
        val DARK_CYAN = fromRgb(0, 139, 139)
        val DARK_GOLDENROD = fromRgb(184, 134, 11)
        val DARK_GRAY = fromRgb(169, 169, 169)
        val DARK_GREEN = fromRgb(0, 100, 0)
        val DARK_KHAKI = fromRgb(189, 183, 107)
        val DARK_MAGENTA = fromRgb(139, 0, 139)
        val DARK_OLIVE_GREEN = fromRgb(85, 107, 47)
        val DARK_ORANGE = fromRgb(255, 140, 0)
        val DARK_ORCHID = fromRgb(153, 50, 204)
        val DARK_RED = fromRgb(139, 0, 0)
        val DARK_SALMON = fromRgb(233, 150, 122)
        val DARK_SEA_GREEN = fromRgb(143, 188, 139)
        val DARK_SLATE_BLUE = fromRgb(72, 61, 139)
        val DARK_SLATE_GRAY = fromRgb(47, 79, 79)
        val DARK_TURQUOISE = fromRgb(0, 206, 209)
        val DARK_VIOLET = fromRgb(148, 0, 211)
        val DEEP_PINK = fromRgb(255, 20, 147)
        val DEEP_SKY_BLUE = fromRgb(0, 191, 255)
        val DIM_GRAY = fromRgb(105, 105, 105)
        val DODGER_BLUE = fromRgb(30, 144, 255)
        val FIREBRICK = fromRgb(178, 34, 34)
        val FLORAL_WHITE = fromRgb(255, 250, 240)
        val FOREST_GREEN = fromRgb(34, 139, 34)
        val FUCHSIA = fromRgb(255, 0, 255)
        val GAINSBORO = fromRgb(220, 220, 220)
        val GHOST_WHITE = fromRgb(248, 248, 255)
        val GOLD = fromRgb(255, 215, 0)
        val GOLDENROD = fromRgb(218, 165, 32)
        val GRAY = fromRgb(128, 128, 128)
        val GREEN = fromRgb(0, 128, 0)
        val GREEN_YELLOW = fromRgb(173, 255, 47)
        val HONEYDEW = fromRgb(240, 255, 240)
        val HOT_PINK = fromRgb(255, 105, 180)
        val INDIAN_RED = fromRgb(205, 92, 92)
        val INDIGO = fromRgb(75, 0, 130)
        val IVORY = fromRgb(255, 255, 240)
        val KHAKI = fromRgb(240, 230, 140)
        val LAVENDER = fromRgb(230, 230, 250)
        val LAVENDER_BLUSH = fromRgb(255, 240, 245)
        val LAWN_GREEN = fromRgb(124, 252, 0)
        val LEMON_CHIFFON = fromRgb(255, 250, 205)
        val LIGHT_BLUE = fromRgb(173, 216, 230)
        val LIGHT_CORAL = fromRgb(240, 128, 128)
        val LIGHT_CYAN = fromRgb(224, 255, 255)
        val LIGHT_GOLDENROD_YELLOW = fromRgb(250, 250, 210)
        val LIGHT_GRAY = fromRgb(211, 211, 211)
        val LIGHT_GREEN = fromRgb(144, 238, 144)
        val LIGHT_PINK = fromRgb(255, 182, 193)
        val LIGHT_SALMON = fromRgb(255, 160, 122)
        val LIGHT_SEA_GREEN = fromRgb(32, 178, 170)
        val LIGHT_SKY_BLUE = fromRgb(135, 206, 250)
        val LIGHT_SLATE_GRAY = fromRgb(119, 136, 153)
        val LIGHT_STEEL_BLUE = fromRgb(176, 196, 222)
        val LIGHT_YELLOW = fromRgb(255, 255, 224)
        val LIME = fromRgb(0, 255, 0)
        val LIME_GREEN = fromRgb(50, 205, 50)
        val LINEN = fromRgb(250, 240, 230)
        val MAGENTA = fromRgb(255, 0, 255)
        val MAROON = fromRgb(128, 0, 0)
        val MEDIUM_AQUAMARINE = fromRgb(102, 205, 170)
        val MEDIUM_BLUE = fromRgb(0, 0, 205)
        val MEDIUM_ORCHID = fromRgb(186, 85, 211)
        val MEDIUM_PURPLE = fromRgb(147, 112, 219)
        val MEDIUM_SEA_GREEN = fromRgb(60, 179, 113)
        val MEDIUM_SLATE_BLUE = fromRgb(123, 104, 238)
        val MEDIUM_SPRING_GREEN = fromRgb(0, 250, 154)
        val MEDIUM_TURQUOISE = fromRgb(72, 209, 204)
        val MEDIUM_VIOLET_RED = fromRgb(199, 21, 133)
        val MIDNIGHT_BLUE = fromRgb(25, 25, 112)
        val MINT_CREAM = fromRgb(245, 255, 250)
        val MISTY_ROSE = fromRgb(255, 228, 225)
        val MOCCASIN = fromRgb(255, 228, 181)
        val NAVAJO_WHITE = fromRgb(255, 222, 173)
        val NAVY = fromRgb(0, 0, 128)
        val OLD_LACE = fromRgb(253, 245, 230)
        val OLIVE = fromRgb(128, 128, 0)
        val OLIVE_DRAB = fromRgb(107, 142, 35)
        val ORANGE = fromRgb(255, 165, 0)
        val ORANGE_RED = fromRgb(255, 69, 0)
        val ORCHID = fromRgb(218, 112, 214)
        val PALE_GOLDENROD = fromRgb(238, 232, 170)
        val PALE_GREEN = fromRgb(152, 251, 152)
        val PALE_TURQUOISE = fromRgb(175, 238, 238)
        val PALE_VIOLET_RED = fromRgb(219, 112, 147)
        val PAPAYA_WHIP = fromRgb(255, 239, 213)
        val PEACH_PUFF = fromRgb(255, 218, 185)
        val PERU = fromRgb(205, 133, 63)
        val PINK = fromRgb(255, 192, 203)
        val PLUM = fromRgb(221, 160, 221)
        val POWDER_BLUE = fromRgb(176, 224, 230)
        val PURPLE = fromRgb(128, 0, 128)
        val RED = fromRgb(255, 0, 0)
        val ROSY_BROWN = fromRgb(188, 143, 143)
        val ROYAL_BLUE = fromRgb(65, 105, 225)
        val SADDLE_BROWN = fromRgb(139, 69, 19)
        val SALMON = fromRgb(250, 128, 114)
        val SANDY_BROWN = fromRgb(244, 164, 96)
        val SEA_GREEN = fromRgb(46, 139, 87)
        val SEA_SHELL = fromRgb(255, 245, 238)
        val SIENNA = fromRgb(160, 82, 45)
        val SILVER = fromRgb(192, 192, 192)
        val SKY_BLUE = fromRgb(135, 206, 235)
        val SLATE_BLUE = fromRgb(106, 90, 205)
        val SLATE_GRAY = fromRgb(112, 128, 144)
        val SNOW = fromRgb(255, 250, 250)
        val SPRING_GREEN = fromRgb(0, 255, 127)
        val STEEL_BLUE = fromRgb(70, 130, 180)
        val TAN = fromRgb(210, 180, 140)
        val TEAL = fromRgb(0, 128, 128)
        val THISTLE = fromRgb(216, 191, 216)
        val TOMATO = fromRgb(255, 99, 71)
        val TURQUOISE = fromRgb(64, 224, 208)
        val VIOLET = fromRgb(238, 130, 238)
        val WHEAT = fromRgb(245, 222, 179)
        val WHITE = fromRgb(255, 255, 255)
        val WHITE_SMOKE = fromRgb(245, 245, 245)
        val YELLOW = fromRgb(255, 255, 0)
        val YELLOW_GREEN = fromRgb(154, 205, 50)
    }
}
