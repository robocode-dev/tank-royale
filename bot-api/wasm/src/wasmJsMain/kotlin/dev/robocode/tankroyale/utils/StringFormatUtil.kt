package dev.robocode.tankroyale.utils

/**
 * Utility class for string formatting operations compatible with Kotlin/WASM
 */
object StringFormatUtil {
    /**
     * Formats an integer as a 2-digit hexadecimal string with uppercase letters
     */
    fun toHex2(value: Int): String {
        val hex = value.toString(16).uppercase()
        return if (hex.length == 1) "0$hex" else hex
    }

    /**
     * Formats a color string representation similar to String.format for RGB
     */
    fun formatRgbColor(r: Int, g: Int, b: Int): String {
        return "Color(r=$r, g=$g, b=$b)"
    }

    /**
     * Formats a color string representation similar to String.format for RGBA
     */
    fun formatRgbaColor(r: Int, g: Int, b: Int, a: Int): String {
        return "Color(r=$r, g=$g, b=$b, a=$a)"
    }

    /**
     * Formats a hexadecimal color string for RGB
     */
    fun formatHexRgb(r: Int, g: Int, b: Int): String {
        return "#${toHex2(r)}${toHex2(g)}${toHex2(b)}"
    }

    /**
     * Formats a hexadecimal color string for RGBA
     */
    fun formatHexRgba(r: Int, g: Int, b: Int, a: Int): String {
        return "#${toHex2(r)}${toHex2(g)}${toHex2(b)}${toHex2(a)}"
    }
}
