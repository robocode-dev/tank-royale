package model

import dev.robocode.tankroyale.server.model.Point
import dev.robocode.tankroyale.server.rules.calcBulletSpeed
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import model.factory.BulletFactory
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

class IBulletTest : StringSpec({

    "speed() must be based on calcBulletSpeed()" {
        forAll(
            row(1.0), row(1.5), row(2.4), row(3.2), row(4.7), row(6.8),
        ) { power ->
            BulletFactory.createBullet(power = power).speed() shouldBe calcBulletSpeed(power)
        }
    }

    "position() must be based on startPosition, direction, power and tick" {
        forAll(
             row(
                Point(0.0, 0.0), 0.0, speedToFirepower(20.0 - 2), 0,
                Point(0.0 + 0 * cos(toRadians(0.0)) * (20.0 - 2), 0.0 + 0 * sin(toRadians(0.0)) * (20.0 - 2))
            ),
            row(
                Point(1.2, 3.4), 45.0, speedToFirepower(20.0 - 4), 1,
                Point(1.2 + 1 * cos(toRadians(45.0)) * (20.0 - 4), 3.4 + 1 * sin(toRadians(45.0)) * (20.0 - 4))
            ),
            row(
                Point(300.0, 400.0), 321.45, speedToFirepower(20.0 - 8), 23,
                Point(300.0 + 23 * cos(toRadians(321.45)) * (20.0 - 8), 400.0 + 23 * sin(toRadians(321.45)) * (20.0 - 8))
            )
        ) { startPosition, direction, power, tick, expectedPosition ->
            BulletFactory.createBullet(
                startPosition = startPosition, direction = direction, power = power, tick = tick
            ).position() shouldBe expectedPosition
        }
    }

    "nextPosition() must be based on startPosition, direction, power and tick + 1" {
        forAll(
            row(
                Point(0.0, 0.0), 0.0, speedToFirepower(20.0 - 2), 0,
                Point(0.0 + (0 + 1) * cos(toRadians(0.0)) * (20.0 - 2), 0.0 + (0 + 1) * sin(toRadians(0.0)) * (20.0 - 2))
            ),
            row(
                Point(1.2, 3.4), 45.0, speedToFirepower(20.0 - 4), 1,
                Point(1.2 + (1 + 1) * cos(toRadians(45.0)) * (20.0 - 4), 3.4 + (1 + 1) * sin(toRadians(45.0)) * (20.0 - 4))
            ),
            row(
                Point(300.0, 400.0), 321.45, speedToFirepower(20.0 - 8), 23,
                Point(300.0 + (23 + 1) * cos(toRadians(321.45)) * (20.0 - 8), 400.0 + (23 + 1) * sin(toRadians(321.45)) * (20.0 - 8))
            )
        ) { startPosition, direction, power, tick, nextPosition ->
            BulletFactory.createBullet(
                startPosition = startPosition, direction = direction, power = power, tick = tick
            ).nextPosition() shouldBe nextPosition
        }
    }
})

private fun speedToFirepower(speed: Double) = (20.0 - speed) / 3