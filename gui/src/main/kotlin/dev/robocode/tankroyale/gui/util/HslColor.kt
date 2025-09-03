package dev.robocode.tankroyale.gui.util

import java.awt.Color

/**
 * Data class for holding a HSL color.
 */
data class HslColor(var hue: Float, var saturation: Float, var lightness: Float) {

    /**
     * Multiply a factor to the HSL lightning value.
     * @param factor is the factor to multiply to the lightning value.
     */
    fun multLight(factor: Float): HslColor {
        lightness *= factor
        return this
    }

    /**
     * Add a value to the HSL lightning value.
     * @param value is the value to add to the lightning value.
     */
    fun addLight(value: Float): HslColor {
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
