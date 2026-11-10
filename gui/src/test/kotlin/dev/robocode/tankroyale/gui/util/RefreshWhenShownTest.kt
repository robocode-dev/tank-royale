package dev.robocode.tankroyale.gui.util

import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RefreshWhenShownTest : StringSpec({
    tags(Tag("Unit"))

    "a change while the screen is showing refreshes it at once" {
        var refreshed = 0
        val refresh = RefreshWhenShown({ true }) { refreshed++ }

        refresh.changed()
        refresh.shown()

        refreshed shouldBe 1
    }

    "changes while the screen is not showing do no work until it is shown, and then once" {
        var showing = false
        var refreshed = 0
        val refresh = RefreshWhenShown({ showing }) { refreshed++ }

        refresh.changed()
        refresh.changed()
        refreshed shouldBe 0

        showing = true
        refresh.shown()
        refresh.shown()
        refreshed shouldBe 1
    }

    "showing the screen without a change does not refresh it" {
        var refreshed = 0
        RefreshWhenShown({ false }) { refreshed++ }.shown()

        refreshed shouldBe 0
    }
})
