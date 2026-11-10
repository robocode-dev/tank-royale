package dev.robocode.tankroyale.gui.util

import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RefreshWhenShownTest : StringSpec({
    tags(Tag("Unit"))

    "a change while the screen is showing refreshes it at once" {
        var refreshed = 0
        RefreshWhenShown({ true }) { refreshed++ }.changed()

        refreshed shouldBe 1
    }

    "a change while the screen is not showing does no work" {
        var refreshed = 0
        RefreshWhenShown({ false }) { refreshed++ }.changed()

        refreshed shouldBe 0
    }

    "the screen is refreshed each time it is shown, also when nothing it was told about changed" {
        var refreshed = 0
        val refresh = RefreshWhenShown({ false }) { refreshed++ }

        refresh.shown()
        refresh.shown()

        refreshed shouldBe 2
    }
})
