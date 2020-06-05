package dev.robocode.tankroyale.ui.desktop.ui.extensions

import java.awt.Color

data class Hsl(var hue: Float, var saturation: Float, var lightness: Float) {

    fun multLight(factor: Float): Hsl {
        lightness *= factor
        return this
    }

    fun addLight(value: Float): Hsl {
        lightness += value
        if (lightness < 0f) lightness = 0f
        else if (lightness > 1f) lightness = 1f
        return this
    }

    /**
     * Creates a new Color from a HSL color value.
     *
     * @return The converted Color.
     */
    fun toColor(): Color {
        val r: Float
        val g: Float
        val b: Float
        if (saturation == 0f) {
            b = lightness
            g = b
            r = g // achromatic
        } else {
            val q = if (lightness < 0.5f)
                lightness * (1 + saturation)
            else
                lightness + saturation - lightness * saturation
            val p = 2 * lightness - q
            r = hueToRgb(p, q, hue + 1f / 3f)
            g = hueToRgb(p, q, hue)
            b = hueToRgb(p, q, hue - 1f / 3f)
        }
        return Color(to255(r), to255(g), to255(b))
    }

    private fun to255(v: Float): Int {
        return 255f.coerceAtMost(256 * v).toInt()
    }

    /** Helper method that converts hue to rgb  */
    private fun hueToRgb(m1: Float, m2: Float, hue: Float): Float {
        var t = hue
        if (t < 0f) t++
        if (t > 1f) t--
        if (t < 1f / 6f) return m1 + (m2 - m1) * 6f * t
        if (t < 1f / 2f) return m2
        return if (t < 2f / 3f) m1 + (m2 - m1) * (2f / 3f - t) * 6f else m1
    }
}

object ColorExt {
    /**
     * Converts this Color into a HSL color value.
     *
     * @return The HSL representation of this color.
     */
    fun Color.toHsl(): Hsl {
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
    private fun rgbToHsl(red: Int, green: Int, blue: Int): Hsl {
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
        return Hsl(h, s, l)
    }
}