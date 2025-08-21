package dev.robocode.tankroyale.botapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [InitialPosition.fromString] and [InitialPosition.toString].
 *
 * Converted from the original JUnit/AssertJ Java tests to Kotlin/WASM using the
 * Kotlin test library.
 */
class InitialPositionTest {

    @Test
    fun givenValidInputString_whenCallingFromString_thenReturnedPositionFieldsMustBeSame() {
        for ((str, x, y, angle) in fromStringData()) {
            val pos = InitialPosition.fromString(str)
            assertEquals(x, pos?.x, "x mismatch for input: \"$str\"")
            assertEquals(y, pos?.y, "y mismatch for input: \"$str\"")
            assertEquals(angle, pos?.direction, "direction mismatch for input: \"$str\"")
        }
    }

    @Test
    fun givenEmptyOrBlankInputString_whenCallingFromString_thenReturnNull() {
        for (str in blankStrings()) {
            val pos = InitialPosition.fromString(str)
            assertNull(pos, "Expected null for blank input: \"$str\"")
        }
    }

    @Test
    fun givenValidInputString_whenCallingToString_thenReturnedStringIsFormattedAndMatchesInputString() {
        for ((input, expected) in toStringData()) {
            val pos = InitialPosition.fromString(input)
            if (pos == null) {
                assertEquals("", expected, "Expected empty string for input: \"$input\"")
            } else {
                assertEquals(expected, pos.toString(), "String representation mismatch for input: \"$input\"")
            }
        }
    }

    // --- Test data providers ---

    private fun fromStringData(): List<Quadruple<String, Double?, Double?, Double?>> = listOf(
        Quadruple("50,50, 90", 50.0, 50.0, 90.0),
        Quadruple("12.23, -123.3, 45.5", 12.23, -123.3, 45.5),
        Quadruple(" 50 ", 50.0, null, null),
        Quadruple(" 50.1  70.2 ", 50.1, 70.2, null),
        Quadruple("50.1 70.2, 678.3", 50.1, 70.2, 678.3),
        Quadruple("50.1  , 70.2, 678.3", 50.1, 70.2, 678.3),
        Quadruple("50.1 70.2, 678.3 789.1", 50.1, 70.2, 678.3),
        Quadruple("50.1  , , 678.3", 50.1, null, 678.3),
        Quadruple(", , 678.3", null, null, 678.3)
    )

    private fun blankStrings(): List<String> = listOf(
        "",
        " \t",
        " ,",
        ",,,",
        ", ,"
    )

    private fun toStringData(): List<Pair<String, String>> = listOf(
        "50, 50, 90" to "50.0,50.0,90.0",
        "12.23, -123.3, 45.5" to "12.23,-123.3,45.5",
        " 50 " to "50.0,,",
        " 50.1  70.2 " to "50.1,70.2,",
        "50.1 70.2, 678.3" to "50.1,70.2,678.3",
        "50.1  , 70.2, 678.3" to "50.1,70.2,678.3",
        "50.1 70.2, 678.3 789.1" to "50.1,70.2,678.3",
        "50.1  , , 678.3" to "50.1,,678.3",
        ", , 678.3" to ",,678.3",
        "" to "",
        " \t" to "",
        " ," to "",
        ",,," to "",
        ", ," to ""
    )

    // Simple data holder to avoid importing external tuple classes
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}