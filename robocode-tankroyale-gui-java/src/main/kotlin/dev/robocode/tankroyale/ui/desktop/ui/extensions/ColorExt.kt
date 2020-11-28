package dev.robocode.tankroyale.ui.desktop.ui.extensions

import dev.robocode.tankroyale.ui.desktop.util.HslColor
import java.awt.Color

/**
 * Extends the java.awt.Color class additional functionality.
 */
object ColorExt {

    val Color.lightness: Float
        get() = toHsl().lightness

    /**
     * Converts this Color into a HSL color value.
     *
     * @return The HSL representation of this color.
     */
    fun Color.toHsl(): HslColor {
        return rgbToHsl(red, green, blue)
    }

    /**
     * Converts an RGB color value to HSL color value.
     * <p>
     * Conversion formula adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes red, green, and blue are contained in the set [0, 255] and returns h, s, and l in the set [0, 1].
     *
     * @param red is the red color value
     * @param green is the green color value
     * @param blue is the blue color value
     * @return The HSL representation
     */
    private fun rgbToHsl(red: Int, green: Int, blue: Int): HslColor {
        val r = red / 255f
        val g = green / 255f
        val b = blue / 255f
        val max = if (r > g && r > b) r else if (g > b) g else b
        val min = if (r < g && r < b) r else if (g < b) g else b
        var h: Float
        val s: Float
        val l: Float
        l = (max + min) / 2
        if (max == min) {
            s = 0f
            h = s
        } else {
            val d = max - min
            s = if (l > 0.5f) d / (2 - max - min) else d / (max + min)
            h = if (r > g && r > b) (g - b) / d + (if (g < b) 6 else 0)
            else if (g > b) (b - r) / d + 2
            else (r - g) / d + 4
            h /= 6
        }
        return HslColor(h, s, l)
    }
}