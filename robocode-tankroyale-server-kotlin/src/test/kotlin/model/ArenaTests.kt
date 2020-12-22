package model

import dev.robocode.tankroyale.server.model.Arena
import io.kotest.core.spec.style.StringSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe


class ArenaTests : StringSpec({
    "properties are correct" {
        val width = 123
        val height = 987

        val size = Arena(width, height)
        size.width shouldBe width
        size.height shouldBe height
    }

    "properties are read-only" {
        shouldThrow<NoSuchMethodException> {
            Arena::class.java.getDeclaredMethod("setWidth")
        }
        shouldThrow<NoSuchMethodException> {
            Arena::class.java.getDeclaredMethod("setHeight")
        }
    }

    "arenas are equal" {
        val width = 123
        val height = 987

        Arena(width, height) shouldBe Arena(width, height)
    }
})