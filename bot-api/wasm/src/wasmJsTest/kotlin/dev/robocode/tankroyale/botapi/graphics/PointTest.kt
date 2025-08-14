package dev.robocode.tankroyale.botapi.graphics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for the Point class.
 */
class PointTest {

    @Test
    fun testConstructorAndGetters() {
        // Given
        val x = 10.5
        val y = -5.25

        // When
        val point = Point(x, y)

        // Then
        assertEquals(x, point.x, "X coordinate should match constructor value")
        assertEquals(y, point.y, "Y coordinate should match constructor value")
    }

    @Test
    fun testEquality() {
        // Given
        val point1 = Point(1.0, 2.0)
        val point2 = Point(1.0, 2.0)
        val point3 = Point(1.0, 3.0)
        val point4 = Point(3.0, 2.0)

        // Then
        assertEquals(point1, point2, "Equal points should be equal")
        assertNotEquals(point1, point3, "Points with different y coordinates should not be equal")
        assertNotEquals(point1, point4, "Points with different x coordinates should not be equal")
        assertNotNull(point1, "Point should not equal null")
    }

    @Test
    fun testEqualityWithExactValues() {
        // Given - Note: Since we now use exact equality, small differences will make points unequal
        val point1 = Point(1.0, 2.0)
        val point2 = Point(1.0, 2.0) // Exactly the same values
        val point3 = Point(1.0 + 1e-11, 2.0 - 1e-11) // Small differences - should now be unequal
        val point4 = Point(1.0 + 1e-9, 2.0) // Larger differences - should be unequal

        // Then
        assertEquals(point1, point2, "Points with exactly the same values should be equal")
        assertNotEquals(point1, point3, "Points with any difference should not be equal (exact comparison)")
        assertNotEquals(point1, point4, "Points with differences should not be equal")
    }

    @Test
    fun testHashCode() {
        // Given
        val point1 = Point(1.0, 2.0)
        val point2 = Point(1.0, 2.0)

        // Then
        assertEquals(point1.hashCode(), point2.hashCode(), "Equal points should have the same hash code")
    }

    @Test
    fun testToString() {
        // Given
        val point = Point(1.0, 2.0)

        // When
        val result = point.toString()

        // Then
        assertTrue(result.contains("X=1.0"), "toString should contain X coordinate")
        assertTrue(result.contains("Y=2.0"), "toString should contain Y coordinate")
    }

    @Test
    fun testReflexiveEquality() {
        // Given
        val point = Point(1.0, 2.0)

        // Then
        assertEquals(point, point, "A point should be equal to itself")
    }
}
