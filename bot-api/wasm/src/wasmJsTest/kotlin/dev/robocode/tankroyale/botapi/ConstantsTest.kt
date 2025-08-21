package dev.robocode.tankroyale.botapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstantsTest {

    @Test
    fun givenDefinedConstants_whenChecked_thenValuesMatchSpec() {
        assertEquals(18, Constants.BOUNDING_CIRCLE_RADIUS, "BOUNDING_CIRCLE_RADIUS mismatch")
        assertEquals(1200, Constants.SCAN_RADIUS, "SCAN_RADIUS mismatch")

        assertEquals(10, Constants.MAX_TURN_RATE, "MAX_TURN_RATE mismatch")
        assertEquals(20, Constants.MAX_GUN_TURN_RATE, "MAX_GUN_TURN_RATE mismatch")
        assertEquals(45, Constants.MAX_RADAR_TURN_RATE, "MAX_RADAR_TURN_RATE mismatch")

        assertEquals(8, Constants.MAX_SPEED, "MAX_SPEED mismatch")

        assertEquals(0.1, Constants.MIN_FIREPOWER, 1e-12, "MIN_FIREPOWER mismatch")
        assertEquals(3.0, Constants.MAX_FIREPOWER, 1e-12, "MAX_FIREPOWER mismatch")

        assertEquals(1, Constants.ACCELERATION, "ACCELERATION mismatch")
        assertEquals(-2, Constants.DECELERATION, "DECELERATION mismatch")
    }

    @Test
    fun givenComputedConstants_whenEvaluated_thenMatchFormulasAndSanityChecks() {
        val expectedMinBulletSpeed = 20 - 3 * Constants.MAX_FIREPOWER
        val expectedMaxBulletSpeed = 20 - 3 * Constants.MIN_FIREPOWER

        assertEquals(expectedMinBulletSpeed, Constants.MIN_BULLET_SPEED, 1e-12, "MIN_BULLET_SPEED formula mismatch")
        assertEquals(expectedMaxBulletSpeed, Constants.MAX_BULLET_SPEED, 1e-12, "MAX_BULLET_SPEED formula mismatch")

        // Sanity checks
        assertTrue(Constants.MAX_BULLET_SPEED > Constants.MIN_BULLET_SPEED)
        assertEquals(11.0, Constants.MIN_BULLET_SPEED, 1e-12, "Expected MIN_BULLET_SPEED = 11.0")
        assertEquals(19.7, Constants.MAX_BULLET_SPEED, 1e-12, "Expected MAX_BULLET_SPEED = 19.7")
    }
}
