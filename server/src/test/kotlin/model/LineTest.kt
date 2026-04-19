@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package model

import dev.robocode.tankroyale.server.model.Line
import dev.robocode.tankroyale.server.model.Point
import dev.robocode.tankroyale.server.model.isLineIntersectingLine
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe

class LineTest : FunSpec({

    context("TR-SRV-PHY-005: Line intersection").config(tags = setOf(Tag("TR-SRV-PHY-005"))) {

        test("Positive: Crossing lines") {
            val line1 = Line(0.0, 0.0, 10.0, 10.0)
            val line2 = Line(0.0, 10.0, 10.0, 0.0)
            isLineIntersectingLine(line1, line2) shouldBe true
        }

        test("Negative: Parallel lines") {
            val line1 = Line(0.0, 0.0, 10.0, 0.0)
            val line2 = Line(0.0, 5.0, 10.0, 5.0)
            isLineIntersectingLine(line1, line2) shouldBe false
        }

        test("Positive: Coincident lines (overlapping segments)") {
            val line1 = Line(0.0, 0.0, 10.0, 10.0)
            val line2 = Line(5.0, 5.0, 15.0, 15.0)
            isLineIntersectingLine(line1, line2) shouldBe true
        }

        test("Negative: Coincident lines (non-overlapping segments)") {
            val line1 = Line(0.0, 0.0, 5.0, 5.0)
            val line2 = Line(10.0, 10.0, 15.0, 15.0)
            isLineIntersectingLine(line1, line2) shouldBe false
        }

        test("Positive: Endpoint touching") {
            val line1 = Line(0.0, 0.0, 10.0, 10.0)
            val line2 = Line(10.0, 10.0, 20.0, 10.0)
            isLineIntersectingLine(line1, line2) shouldBe true
        }

        test("Positive: One line starts on another line") {
            val line1 = Line(0.0, 0.0, 10.0, 10.0)
            val line2 = Line(5.0, 5.0, 5.0, 10.0)
            isLineIntersectingLine(line1, line2) shouldBe true
        }
    }
})
