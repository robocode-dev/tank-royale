package rules

import dev.robocode.tankroyale.server.rules.calcNewBotSpeed
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData

class mathTest : FunSpec({

    context("calcNewBotSpeed") {
        withData(
            mapOf(
                "current speed = 0, target speed = 0 => new speed = 0" to arrayOf(0.0, 0.0, 0.0),

                // Regular acceleration tests with positive speeds
                "current speed = 0, target speed = 0.5 => new speed = 0.5" to arrayOf(0.0, 0.5, 0.5),
                "current speed = 0, target speed = 1 => new speed = 1" to arrayOf(0.0, 1.0, 1.0),
                "current speed = 0, target speed = 2 => new speed = 1" to arrayOf(0.0, 2.0, 1.0),
                "current speed = 0, target speed = 8 => new speed = 1" to arrayOf(0.0, 8.0, 1.0),
                "current speed = 0, target speed = 9 => new speed = 1" to arrayOf(0.0, 9.0, 1.0),

                "current speed = 4, target speed = 4.5 => new speed = 4.5" to arrayOf(4.0, 4.5, 4.5),
                "current speed = 4, target speed = 5.0 => new speed = 5.0" to arrayOf(4.0, 5.0, 5.0),
                "current speed = 4, target speed = 5.1 => new speed = 5.1" to arrayOf(4.0, 5.0, 5.0),

                "current speed = 7.5, target speed = 9.0 => new speed = 8.0" to arrayOf(7.5, 9.0, 8.0),
                "current speed = 8.0, target speed = 9.0 => new speed = 8.0" to arrayOf(8.0, 9.0, 8.0),

                // Regular acceleration tests with negative speeds
                "current speed = 0, target speed = -0.5 => new speed = -0.5" to arrayOf(0.0, -0.5, -0.5),
                "current speed = 0, target speed = -1 => new speed = -1" to arrayOf(0.0, -1.0, -1.0),
                "current speed = 0, target speed = -2 => new speed = -1" to arrayOf(0.0, -2.0, -1.0),
                "current speed = 0, target speed = -8 => new speed = -1" to arrayOf(0.0, -8.0, -1.0),
                "current speed = 0, target speed = -9 => new speed = -1" to arrayOf(0.0, -9.0, -1.0),

                "current speed = -4, target speed = -4.5 => new speed = -4.5" to arrayOf(-4.0, -4.5, -4.5),
                "current speed = -4, target speed = -5.0 => new speed = -5.0" to arrayOf(-4.0, -5.0, -5.0),
                "current speed = -4, target speed = -5.1 => new speed = -5.1" to arrayOf(-4.0, -5.0, -5.0),

                "current speed = -7.5, target speed = -9.0 => new speed = -8.0" to arrayOf(-7.5, -9.0, -8.0),
                "current speed = -8.0, target speed = -9.0 => new speed = -8.0" to arrayOf(-8.0, -9.0, -8.0),

                // Regular deceleration tests with positive speeds
                "current speed = 8, target speed = 7 => new speed = 7" to arrayOf(8.0, 7.0, 7.0),
                "current speed = 8, target speed = 6.5 => new speed = 6.5" to arrayOf(8.0, 6.5, 6.0),
                "current speed = 8, target speed = 6 => new speed = 6" to arrayOf(8.0, 6.0, 6.0),
                "current speed = 8, target speed = 5 => new speed = 6" to arrayOf(8.0, 5.0, 6.0),

                "current speed = 7, target speed = 6 => new speed = 6" to arrayOf(7.0, 6.0, 6.0),
                "current speed = 7, target speed = 5 => new speed = 5" to arrayOf(7.0, 5.0, 5.0),
                "current speed = 7, target speed = 4 => new speed = 5" to arrayOf(7.0, 4.0, 5.0),

                "current speed = 2, target speed = 1 => new speed = 1" to arrayOf(2.0, 1.0, 1.0),
                "current speed = 2, target speed = 0 => new speed = 0" to arrayOf(2.0, 0.0, 0.0),
                "current speed = 2, target speed = -1 => new speed = 0" to arrayOf(2.0, -1.0, 0.0),

                "current speed = 1, target speed = 0 => new speed = 0" to arrayOf(1.0, 0.0, 0.0),
                "current speed = 1, target speed = -1 => new speed = -0.5" to arrayOf(1.0, -1.0, -0.5),
                "current speed = 1, target speed = -2 => new speed = -0.5" to arrayOf(1.0, -2.0, -0.5),

                // Regular deceleration tests with negative speeds
                "current speed = -8, target speed = -7 => new speed = -7" to arrayOf(-8.0, -7.0, -7.0),
                "current speed = -8, target speed = -6.5 => new speed = -6.5" to arrayOf(-8.0, -6.5, -6.0),
                "current speed = -8, target speed = -6 => new speed = -6" to arrayOf(-8.0, -6.0, -6.0),
                "current speed = -8, target speed = -5 => new speed = -6" to arrayOf(-8.0, -5.0, -6.0),

                "current speed = -7, target speed = -6 => new speed = -6" to arrayOf(-7.0, -6.0, -6.0),
                "current speed = -7, target speed = -5 => new speed = -5" to arrayOf(-7.0, -5.0, -5.0),
                "current speed = -7, target speed = -4 => new speed = -5" to arrayOf(-7.0, -4.0, -5.0),

                "current speed = -2, target speed = -1 => new speed = -1" to arrayOf(-2.0, -1.0, -1.0),
                "current speed = -2, target speed = -0 => new speed = 0" to arrayOf(-2.0, 0.0, 0.0),
                "current speed = -2, target speed = 1 => new speed = 0" to arrayOf(-2.0, 1.0, 0.0),

                "current speed = -1, target speed = 0 => new speed = 0" to arrayOf(-1.0, 0.0, 0.0),
                "current speed = -1, target speed = 1 => new speed = 0.5" to arrayOf(-1.0, 1.0, 0.5),
                "current speed = -1, target speed = 2 => new speed = 0.5" to arrayOf(-1.0, 2.0, 0.5),

                // Speed crossing a positive to negative speed (original Robocode)
                "current speed = 1, target speed = 0 => new speed = 0" to arrayOf(1.0, 0.0, 0.0),
                "current speed = 1, target speed = -8 => new speed = -0.5" to arrayOf(1.0, -8.0, -0.5),
                "current speed = 0.5, target speed = -8 => new speed = -0.75" to arrayOf(0.5, -8.0, -0.75),

                // Speed crossing a negative to positive speed (original Robocode)
                "current speed = -1, target speed = 0 => new speed = 0" to arrayOf(-1.0, 0.0, 0.0),
                "current speed = -1, target speed = 8 => new speed = 0.5" to arrayOf(-1.0, 8.0, 0.5),
                "current speed = -0.5, target speed = 8 => new speed = 0.75" to arrayOf(-0.5, 8.0, 0.75),
            )
        )
        { (currentSpeed, targetSpeed, newSpeed) ->
            calcNewBotSpeed(currentSpeed, targetSpeed) == newSpeed
        }
    }
})