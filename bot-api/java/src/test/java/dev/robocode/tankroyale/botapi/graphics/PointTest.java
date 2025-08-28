package dev.robocode.tankroyale.botapi.graphics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Point} class.
 */
public class PointTest {

    @Test
    public void givenCoordinates_whenConstructingPoint_thenGettersReturnSameValues() {
        // Given
        double x = 10.5;
        double y = -5.25;

        // When
        Point point = new Point(x, y);

        // Then
        assertEquals(x, point.getX(), "X coordinate should match constructor value");
        assertEquals(y, point.getY(), "Y coordinate should match constructor value");
    }

    @Test
    public void givenPoints_whenComparingEquality_thenBehaveAsExpected() {
        // Given
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(1.0, 2.0);
        Point point3 = new Point(1.0, 3.0);
        Point point4 = new Point(3.0, 2.0);

        // Then
        assertEquals(point1, point2, "Equal points should be equal");
        assertNotEquals(point1, point3, "Points with different y coordinates should not be equal");
        assertNotEquals(point1, point4, "Points with different x coordinates should not be equal");
        assertNotEquals(null, point1, "Point should not equal null");
        assertNotEquals("Not a point", point1, "Point should not equal non-point objects");
    }

    @Test
    public void givenEqualPoints_whenHashCode_thenSameHashCode() {
        // Given
        Point point1 = new Point(1.0, 2.0);
        Point point2 = new Point(1.0, 2.0);

        // Then
        assertEquals(point1.hashCode(), point2.hashCode(), "Equal points should have the same hash code");
    }

    @Test
    public void givenPoint_whenToString_thenIncludesXAndYCoordinates() {
        // Given
        Point point = new Point(1.0, 2.0);

        // When
        String result = point.toString();

        // Then
        assertTrue(result.contains("x=1.0"), "toString should contain X coordinate");
        assertTrue(result.contains("y=2.0"), "toString should contain Y coordinate");
    }

    @Test
    public void givenPoint_whenCheckingReflexiveEquality_thenEqualToItself() {
        // Given
        Point point = new Point(1.0, 2.0);

        // Then
        assertEquals(point, point, "A point should be equal to itself");
    }
}
