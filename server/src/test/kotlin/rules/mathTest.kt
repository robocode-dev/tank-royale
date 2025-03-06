package rules

import dev.robocode.tankroyale.server.rules.calcNewBotSpeed
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class mathTest : FunSpec({
    context("calcNewBotSpeed") {
        forAll(
            // Zero speeds
            row("current speed = 0, target speed = 0 => new speed = 0", 0.0, 0.0, 0.0),

            // Regular acceleration tests with positive speeds
            row("current speed = 0, target speed = 0.5 => new speed = 0.5", 0.0, 0.5, 0.5),
            row("current speed = 0, target speed = 1 => new speed = 1", 0.0, 1.0, 1.0),
            row("current speed = 0, target speed = 2 => new speed = 1", 0.0, 2.0, 1.0),
            row("current speed = 0, target speed = 8 => new speed = 1", 0.0, 8.0, 1.0),
            row("current speed = 0, target speed = 9 => new speed = 1", 0.0, 9.0, 1.0),

            row("current speed = 4, target speed = 4.5 => new speed = 4.5", 4.0, 4.5, 4.5),
            row("current speed = 4, target speed = 5.0 => new speed = 5.0", 4.0, 5.0, 5.0),
            row("current speed = 4, target speed = 5.1 => new speed = 5.1", 4.0, 5.0, 5.0),

            row("current speed = 7.5, target speed = 9.0 => new speed = 8.0", 7.5, 9.0, 8.0),
            row("current speed = 8.0, target speed = 9.0 => new speed = 8.0", 8.0, 9.0, 8.0),

            // Regular acceleration tests with negative speeds
            row("current speed = 0, target speed = -0.5 => new speed = -0.5", 0.0, -0.5, -0.5),
            row("current speed = 0, target speed = -1 => new speed = -1", 0.0, -1.0, -1.0),
            row("current speed = 0, target speed = -2 => new speed = -1", 0.0, -2.0, -1.0),
            row("current speed = 0, target speed = -8 => new speed = -1", 0.0, -8.0, -1.0),
            row("current speed = 0, target speed = -9 => new speed = -1", 0.0, -9.0, -1.0),

            row("current speed = -4, target speed = -4.5 => new speed = -4.5", -4.0, -4.5, -4.5),
            row("current speed = -4, target speed = -5.0 => new speed = -5.0", -4.0, -5.0, -5.0),
            row("current speed = -4, target speed = -5.1 => new speed = -5.1", -4.0, -5.0, -5.0),

            row("current speed = -7.5, target speed = -9.0 => new speed = -8.0", -7.5, -9.0, -8.0),
            row("current speed = -8.0, target speed = -9.0 => new speed = -8.0", -8.0, -9.0, -8.0),

            // Regular deceleration tests with positive speeds
            row("current speed = 8, target speed = 7 => new speed = 7", 8.0, 7.0, 7.0),
            row("current speed = 8, target speed = 6.5 => new speed = 6.5", 8.0, 6.5, 6.0),
            row("current speed = 8, target speed = 6 => new speed = 6", 8.0, 6.0, 6.0),
            row("current speed = 8, target speed = 5 => new speed = 6", 8.0, 5.0, 6.0),

            row("current speed = 7, target speed = 6 => new speed = 6", 7.0, 6.0, 6.0),
            row("current speed = 7, target speed = 5 => new speed = 5", 7.0, 5.0, 5.0),
            row("current speed = 7, target speed = 4 => new speed = 5", 7.0, 4.0, 5.0),

            row("current speed = 2, target speed = 1 => new speed = 1", 2.0, 1.0, 1.0),
            row("current speed = 2, target speed = 0 => new speed = 0", 2.0, 0.0, 0.0),
            row("current speed = 2, target speed = -1 => new speed = 0", 2.0, -1.0, 0.0),

            row("current speed = 1, target speed = 0 => new speed = 0", 1.0, 0.0, 0.0),
            row("current speed = 1, target speed = -1 => new speed = -0.5", 1.0, -1.0, -0.5),
            row("current speed = 1, target speed = -2 => new speed = -0.5", 1.0, -2.0, -0.5),

            // Regular deceleration tests with negative speeds
            row("current speed = -8, target speed = -7 => new speed = -7", -8.0, -7.0, -7.0),
            row("current speed = -8, target speed = -6.5 => new speed = -6.5", -8.0, -6.5, -6.0),
            row("current speed = -8, target speed = -6 => new speed = -6", -8.0, -6.0, -6.0),
            row("current speed = -8, target speed = -5 => new speed = -6", -8.0, -5.0, -6.0),

            row("current speed = -7, target speed = -6 => new speed = -6", -7.0, -6.0, -6.0),
            row("current speed = -7, target speed = -5 => new speed = -5", -7.0, -5.0, -5.0),
            row("current speed = -7, target speed = -4 => new speed = -5", -7.0, -4.0, -5.0),

            row("current speed = -2, target speed = -1 => new speed = -1", -2.0, -1.0, -1.0),
            row("current speed = -2, target speed = -0 => new speed = 0", -2.0, 0.0, 0.0),
            row("current speed = -2, target speed = 1 => new speed = 0", -2.0, 1.0, 0.0),

            row("current speed = -1, target speed = 0 => new speed = 0", -1.0, 0.0, 0.0),
            row("current speed = -1, target speed = 1 => new speed = 0.5", -1.0, 1.0, 0.5),
            row("current speed = -1, target speed = 2 => new speed = 0.5", -1.0, 2.0, 0.5),

            // Speed crossing a positive to negative speed (original Robocode)
            row("current speed = 1, target speed = 0 => new speed = 0", 1.0, 0.0, 0.0),
            row("current speed = 1, target speed = -8 => new speed = -0.5", 1.0, -8.0, -0.5),
            row("current speed = 0.5, target speed = -8 => new speed = -0.75", 0.5, -8.0, -0.75),

            // Speed crossing a negative to positive speed (original Robocode)
            row("current speed = -1, target speed = 0 => new speed = 0", -1.0, 0.0, 0.0),
            row("current speed = -1, target speed = 8 => new speed = 0.5", -1.0, 8.0, 0.5),
            row("current speed = -0.5, target speed = 8 => new speed = 0.75", -0.5, 8.0, 0.75)
        ) { _, currentSpeed, targetSpeed, expectedSpeed ->
            calcNewBotSpeed(currentSpeed, targetSpeed) == expectedSpeed
        }
    }
})