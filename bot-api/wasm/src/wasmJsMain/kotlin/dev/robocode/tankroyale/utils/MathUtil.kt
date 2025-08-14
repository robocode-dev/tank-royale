package dev.robocode.tankroyale.utils

import kotlin.math.abs

/**
 * Utility class for mathematical operations compatible with Kotlin/WASM
 */
object MathUtil {
    /**
     * Checks if two double values are equal within epsilon tolerance
     */
    fun doubleEquals(a: Double, b: Double, epsilon: Double = 1e-10): Boolean {
        return abs(a - b) < epsilon
    }
}
