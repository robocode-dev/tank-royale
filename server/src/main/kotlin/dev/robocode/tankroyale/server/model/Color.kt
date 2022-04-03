package dev.robocode.tankroyale.server.model

import java.util.regex.Pattern

private val NUMERIC_RGB: Pattern = Pattern.compile("^#[0-9A-F]{3,6}$", Pattern.CASE_INSENSITIVE)

@JvmInline
value class Color(val value: String) {
    init {
        if (!NUMERIC_RGB.matcher(value).matches())
            throw IllegalArgumentException("The color value is invalid")
    }

    companion object {
        /**
         * Converts a string represented in a numeric format #<red><green><blue> into an integer presentation of the RGB color
         * value in 24-bit. Currently, only numeric representations of colors is supported. Later on, more formats might be
         * supported.
         * <p>
         * Two formats are currently supported, where is RGB color value is either 24-bit (3 x 8-bit color channels) or
         * 12-bit (3 x 4-bit color channels).
         * For example, the saddle brown color can be represented as the 24-bit version "#8B4513", where red, green, blue
         * are the hex values 8B, 45, and 13. The returned integer value will be 0x8B4513 (24-bit format).
         * The same color can also be represented with a 12-bit version in lower color resolution "#941", where red, green,
         * blue are the hex values 9, 4, and 1. The returned integer value will be 0x994411 (24-bit format).
         *
         * @param color is the string representation of a RGB color, e.g. "#8B4513" (24-bit format) or "#941" (12-bit format).
         * @return is a `Color` representing the RGB value in 24-format, e.g. 0x8B4513 or 0x994411.
         */
        fun fromString(color: String?): Color? {
            if (color == null) return null
            val trimmedColor = color.trim()
            if (!NUMERIC_RGB.matcher(trimmedColor).matches()) {
                return null
            }
            return Color(trimmedColor)
        }
    }
}