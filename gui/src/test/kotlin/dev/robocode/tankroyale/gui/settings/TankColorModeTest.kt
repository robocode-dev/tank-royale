package dev.robocode.tankroyale.gui.settings

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TankColorModeTest : StringSpec({
    "fromString() returns correct enum value for each name" {
        TankColorMode.fromString("bot-colors") shouldBe TankColorMode.BOT_COLORS
        TankColorMode.fromString("bot-colors-once") shouldBe TankColorMode.BOT_COLORS_ONCE
        TankColorMode.fromString("default-colors") shouldBe TankColorMode.DEFAULT_COLORS
        TankColorMode.fromString("bot-colors-when-debugging") shouldBe TankColorMode.BOT_COLORS_WHEN_DEBUGGING
    }

    "fromString() is case-insensitive" {
        TankColorMode.fromString("BOT-COLORS") shouldBe TankColorMode.BOT_COLORS
        TankColorMode.fromString("Bot-Colors-Once") shouldBe TankColorMode.BOT_COLORS_ONCE
    }

    "fromString() falls back to BOT_COLORS for unknown/null input" {
        TankColorMode.fromString(null) shouldBe TankColorMode.BOT_COLORS
        TankColorMode.fromString("") shouldBe TankColorMode.BOT_COLORS
        TankColorMode.fromString("unknown") shouldBe TankColorMode.BOT_COLORS
    }
})
