package model

import dev.robocode.tankroyale.server.model.Color
import dev.robocode.tankroyale.server.model.colorStringToRGB
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class ColorTest : StringSpec({

    "colorStringToRGB() tests" {
        forAll (
            row(null, null),
            row("#000000", Color(0x000000)),
            row("#112233", Color(0x112233)),
            row("#ABCDEF", Color(0xABCDEF)),
            row("#000", Color(0x000000)),
            row("#123", Color(0x112233)),
            row("#FFF", Color(0xFFFFFF)),
            row("123", null),
            row("#1234", null),
            row("#1234567", null)

        ) { colorStr, color ->
            colorStringToRGB(colorStr) shouldBe color
        }
    }
})