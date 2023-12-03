package dev.robocode.tankroyale.gui.util

import java.awt.Color
import java.util.regex.Pattern

class ColorUtil {
    companion object {
        private val NUMERIC_RGB = Pattern.compile("^#[0-9a-fA-F]{3,6}$")

        fun fromString(str: String): Color {
            require(NUMERIC_RGB.matcher(str).matches()) { "Illegal color format: $str" }
            val r = Integer.valueOf(str.substring(1, 3), 16)
            val g = Integer.valueOf(str.substring(3, 5), 16)
            val b = Integer.valueOf(str.substring(5, 7), 16)
            return Color(r, g, b)
        }
    }
}