package model

import dev.robocode.tankroyale.server.model.MutablePoint
import dev.robocode.tankroyale.server.model.Point
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.types.shouldBeInstanceOf

class PointTests : StringSpec({
    "properties are correct" {
        val x = 123.456
        val y = 987.654

        val point = Point(x, y)
        point.x shouldBe x
        point.y shouldBe y
    }

    "properties are read-only" {
        shouldThrow<NoSuchMethodException> {
            Point::class.java.getDeclaredMethod("setX")
        }
        shouldThrow<NoSuchMethodException> {
            Point::class.java.getDeclaredMethod("setY")
        }
    }

    "points are equal" {
        val x = 123.456
        val y = 987.654

        Point(x, y) shouldBe Point(x, y)
    }

    "toMutablePoint() returns a MutablePoint that is a copy of the Point" {
        val x = 123.456
        val y = 987.654

        val mutablePoint = Point(x, y).toMutablePoint()
        mutablePoint.shouldBeInstanceOf<MutablePoint>()

        mutablePoint.x shouldBe x
        mutablePoint.y shouldBe y
    }
})