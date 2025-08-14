package dev.robocode.tankroyale.utils

import kotlin.math.round

/**
 * Simple number-formatting helper that mimics Java's DecimalFormat("0.###")
 * without relying on java.text, which is unavailable on Kotlin/WASM.
 */
object NumberFormatUtil {

    /**
     * Formats [value] to a string with max three fractional digits,
     * trimming trailing zeros and an eventual trailing dot.
     */
    fun format(value: Double): String {
        // Round to 3 decimals
        val scaled = round(value * 1000.0) / 1000.0
        var s = scaled.toString()
        // Kotlin/WASM prints integral numbers without ".0", so add if needed
        if (!s.contains('.')) return s
        // Trim trailing zeros
        s = s.trimEnd('0')
        // Remove trailing dot if all zeros were removed
        return if (s.endsWith('.')) s.dropLast(1) else s
    }
}
