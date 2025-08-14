package dev.robocode.tankroyale.utils

/**
 * Utility class for hash code calculations compatible with Kotlin/WASM
 */
object HashUtil {
    /**
     * Calculates hash code for two double values similar to Objects.hash()
     */
    fun hash(x: Double, y: Double): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
