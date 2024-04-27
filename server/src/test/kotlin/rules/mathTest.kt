package rules

import dev.robocode.tankroyale.server.rules.calcNewBotSpeed
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe


class mathTest : FunSpec({

    context("calcNewBotSpeed") {
        withData(
            mapOf(
                // When starting speed is 0 with various positive target speeds, the new speed should not exceed the
                // positive acceleration of 1
                "current speed = 0, target speed = 0 => new speed = 0" to arrayOf(0.0, 0.0, 0.0),
                "current speed = 0, target speed = 0.5 => new speed = 0.5" to arrayOf(0.0, 0.5, 0.5),
                "current speed = 0, target speed = 1 => new speed = 1" to arrayOf(0.0, 1.0, 1.0),
                // New speed cannot pass the acceleration of 1
                "current speed = 0, target speed = 2 => new speed = 1" to arrayOf(0.0, 2.0, 1.0),
                "current speed = 0, target speed = 8 => new speed = 1" to arrayOf(0.0, 8.0, 1.0),
                "current speed = 0, target speed = 9 => new speed = 1" to arrayOf(0.0, 9.0, 1.0),

                // When starting speed is 0 with various negative target speeds, the new speed should not exceed the
                // negative acceleration of 1
                "current speed = 0, target speed = -0.5 => new speed = -0.5" to arrayOf(0.0, -0.5, -0.5),
                "current speed = 0, target speed = -1 => new speed = -1" to arrayOf(0.0, -1.0, -1.0),
                "current speed = 0, target speed = -2 => new speed = -1" to arrayOf(0.0, -2.0, -1.0),
                // New speed cannot pass the acceleration of -1
                "current speed = 0, target speed = -8 => new speed = -1" to arrayOf(0.0, -8.0, -1.0),
                "current speed = 0, target speed = -9 => new speed = -1" to arrayOf(0.0, -9.0, -1.0),

                // Decelerating from max speed should allow deceleration of down to -2
                "current speed = 8, target speed = 7 => new speed = 7" to arrayOf(8.0, 7.0, 7.0),
                "current speed = 8, target speed = 6 => new speed = 6" to arrayOf(8.0, 6.0, 6.0),
                // new speed cannot pass the deceleration of -2
                "current speed = 8, target speed = 5 => new speed = 6" to arrayOf(8.0, 5.0, 6.0),

                // Decelerating from min speed should allow deceleration of up to 2
                "current speed = -8, target speed = -7 => new speed = -7" to arrayOf(-8.0, -7.0, -7.0),
                "current speed = -8, target speed = -6 => new speed = -6" to arrayOf(-8.0, -6.0, -6.0),
                // new speed cannot pass the deceleration of -2
                "current speed = -8, target speed = -5 => new speed = -6" to arrayOf(-8.0, -5.0, -6.0),

                // Speed crossing a positive to negative speed (original Robocode)
                "current speed = 1, target speed = -8 => new speed = -0.5" to arrayOf(1.0, -8.0, -0.5),
                "current speed = 0.5, target speed = -8 => new speed = -0.75" to arrayOf(0.5, -8.0, -0.75),

                // Speed crossing a negative to positive speed (original Robocode)
                "current speed = -1, target speed = 8 => new speed = 0.5" to arrayOf(-1.0, 8.0, 0.5),
                "current speed = -0.5, target speed = 8 => new speed = 0.75" to arrayOf(-0.5, 8.0, 0.75),
            )
        )
        { (currentSpeed, targetSpeed, newSpeed) ->
            calcNewBotSpeed(currentSpeed, targetSpeed) shouldBe newSpeed
        }
    }
})