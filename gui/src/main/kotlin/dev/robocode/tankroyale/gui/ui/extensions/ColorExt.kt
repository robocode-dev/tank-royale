package dev.robocode.tankroyale.gui.ui.extensions

import dev.robocode.tankroyale.gui.util.HslColor
import java.awt.Color

/**
 * Extends the java.awt.Color class additional functionality.
 */
object ColorExt {

    val Color.lightness: Float
        get() = hsl.lightness

    val Color.web: String
        get() = "#%02x%02x%02x".format(red, green, blue)

    val Color.hsl: HslColor
        get() = rgbToHsl(red, green, blue)

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

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)

        val l = (max + min) / 2

        val s = if (max == min) 0f else if (l > 0.5f) (max - min) / (2 - max - min) else (max - min) / (max + min)

        val h = when (max) {
            r -> when (min) {
                g -> (g - b) / (max - min) + (if (g < b) 6 else 0)
                else -> (b - r) / (max - min) + 2
            }

            g -> {
                (b - r) / (max - min) + 4
            }

            else -> {
                (r - g) / (max - min) + 6
            }
        }
        return HslColor(h, s, l)
    }
}