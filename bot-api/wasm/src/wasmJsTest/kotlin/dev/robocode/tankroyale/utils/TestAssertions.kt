package dev.robocode.tankroyale.utils

/**
 * Utility class for test assertions compatible with Kotlin/WASM
 */
object TestAssertions {
    /**
     * Asserts that two values are equal
     */
    fun assertEquals(expected: Any?, actual: Any?, message: String = "") {
        if (expected != actual) {
            val msg = if (message.isEmpty()) 
                "Expected: $expected, but was: $actual" 
            else 
                "$message - Expected: $expected, but was: $actual"
            throw AssertionError(msg)
        }
    }

    /**
     * Asserts that two values are not equal
     */
    fun assertNotEquals(unexpected: Any?, actual: Any?, message: String = "") {
        if (unexpected == actual) {
            val msg = if (message.isEmpty()) 
                "Expected values to be different, but both were: $actual" 
            else 
                "$message - Expected values to be different, but both were: $actual"
            throw AssertionError(msg)
        }
    }
}
