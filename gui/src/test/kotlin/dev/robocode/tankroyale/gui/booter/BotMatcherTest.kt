package dev.robocode.tankroyale.gui.booter

import dev.robocode.tankroyale.client.model.BotInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

class BotMatcherTest : FunSpec({

    fun makeBotInfo(name: String, version: String): BotInfo = BotInfo(
        name = name,
        version = version,
        authors = emptyList(),
        countryCodes = emptyList(),
        gameTypes = emptySet(),
        host = "localhost",
        port = 50000,
    )

    context("BotMatcher") {

        test("all bots connected returns isComplete = true") {
            val matcher = BotMatcher(
                listOf(BotIdentity("BotA", "1.0"), BotIdentity("BotB", "2.0"))
            )
            matcher.update(listOf(makeBotInfo("BotA", "1.0"), makeBotInfo("BotB", "2.0")))
            matcher.isComplete shouldBe true
        }

        test("partial connection returns isComplete = false") {
            val matcher = BotMatcher(
                listOf(BotIdentity("BotA", "1.0"), BotIdentity("BotB", "2.0"))
            )
            matcher.update(listOf(makeBotInfo("BotA", "1.0")))
            matcher.isComplete shouldBe false
        }

        test("partial connection shows correct counts per identity") {
            val matcher = BotMatcher(
                listOf(
                    BotIdentity("Droid", "1.0"),
                    BotIdentity("Droid", "1.0"),
                    BotIdentity("Droid", "1.0"),
                    BotIdentity("Droid", "1.0"),
                )
            )
            matcher.update(listOf(makeBotInfo("Droid", "1.0"), makeBotInfo("Droid", "1.0")))
            matcher.connected[BotIdentity("Droid", "1.0")] shouldBe 2
            matcher.pending[BotIdentity("Droid", "1.0")] shouldBe 2
            matcher.isComplete shouldBe false
        }

        test("stray bot with different identity is ignored") {
            val matcher = BotMatcher(listOf(BotIdentity("BotA", "1.0")))
            matcher.update(listOf(makeBotInfo("BotA", "1.0"), makeBotInfo("StrayBot", "9.9")))
            matcher.isComplete shouldBe true
            matcher.connected.containsKey(BotIdentity("StrayBot", "9.9")) shouldBe false
        }

        test("no bots connected returns isComplete = false and all pending") {
            val matcher = BotMatcher(listOf(BotIdentity("BotA", "1.0"), BotIdentity("BotB", "2.0")))
            matcher.update(emptyList())
            matcher.isComplete shouldBe false
            matcher.pending[BotIdentity("BotA", "1.0")] shouldBe 1
            matcher.pending[BotIdentity("BotB", "2.0")] shouldBe 1
        }

        test("all bots connected clears pending") {
            val matcher = BotMatcher(listOf(BotIdentity("BotA", "1.0")))
            matcher.update(listOf(makeBotInfo("BotA", "1.0")))
            matcher.pending.shouldBeEmpty()
        }

        test("duplicate expected identities require matching count") {
            val matcher = BotMatcher(
                listOf(BotIdentity("Droid", "1.0"), BotIdentity("Droid", "1.0"))
            )
            // Only one connected — not complete
            matcher.update(listOf(makeBotInfo("Droid", "1.0")))
            matcher.isComplete shouldBe false

            // Two connected — complete
            matcher.update(listOf(makeBotInfo("Droid", "1.0"), makeBotInfo("Droid", "1.0")))
            matcher.isComplete shouldBe true
        }
    }
})
