package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConstantsTest {

    private static final double EPS = 1e-10;

    @Test
    public void givenDefinedConstants_whenChecked_thenValuesMatchSpec() {
        assertEquals(18, Constants.BOUNDING_CIRCLE_RADIUS);
        assertEquals(1200, Constants.SCAN_RADIUS);
        assertEquals(10, Constants.MAX_TURN_RATE);
        assertEquals(20, Constants.MAX_GUN_TURN_RATE);
        assertEquals(45, Constants.MAX_RADAR_TURN_RATE);
        assertEquals(8, Constants.MAX_SPEED);

        assertEquals(0.1, Constants.MIN_FIREPOWER, EPS);
        assertEquals(3.0, Constants.MAX_FIREPOWER, EPS);

        // Bullet speed by formula
        assertEquals(20 - 3 * Constants.MAX_FIREPOWER, Constants.MIN_BULLET_SPEED, EPS);
        assertEquals(20 - 3 * Constants.MIN_FIREPOWER, Constants.MAX_BULLET_SPEED, EPS);
        // And explicit numeric values to avoid self-referential checks
        assertEquals(11.0, Constants.MIN_BULLET_SPEED, EPS);
        assertEquals(19.7, Constants.MAX_BULLET_SPEED, EPS);

        assertEquals(1, Constants.ACCELERATION);
        assertEquals(-2, Constants.DECELERATION);
    }
}
