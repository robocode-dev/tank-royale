package model

import dev.robocode.tankroyale.server.model.Color
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ColorTest : StringSpec({
    "#09F is valid" {
        Color.from("#09F")?.value shouldBe "#0099FF"
    }

    "#09f is valid" {
        Color.from("#09a")?.value shouldBe "#0099AA"
    }

    "#1234CE is valid" {
        Color.from("#1234CE")?.value shouldBe "#1234CE"
    }

    "#abcdef is valid" {
        Color.from("#abcdef")?.value shouldBe "#ABCDEF"
    }

    "abcdef should throw IllegalArgumentException" {
        shouldThrow<IllegalArgumentException> {
            Color.from("abcdef")
        }
    }

    "#ab should throw IllegalArgumentException" {
        shouldThrow<IllegalArgumentException> {
            Color.from("#ab")
        }
    }

    "#abcd should throw IllegalArgumentException" {
        shouldThrow<IllegalArgumentException> {
            Color.from("#abcd")
        }
    }

    "#xxxxxx should throw IllegalArgumentException" {
        shouldThrow<IllegalArgumentException> {
            Color.from("#xxxxxx")
        }
    }
})
