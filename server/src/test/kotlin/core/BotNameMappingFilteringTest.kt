package core

import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BotNameMappingFilteringTest : FunSpec({
    tags(Tag("PRO-010a"))
    tags(Tag("Integration"))
    tags(Tag("Negative"))

    test("testPRO010a_IntegrationNegative_omitsOpponentNames") {
        val events = startBattleAndCaptureEvents()

        events.getValue("session-a").botNames.map { it.botId }.toSet() shouldBe setOf(1, 3, 4)
        events.getValue("session-b").botNames.map { it.botId }.toSet() shouldBe setOf(2)
        events.getValue("session-c").botNames.map { it.botId }.toSet() shouldBe setOf(1, 3, 4)
        events.getValue("session-d").botNames.map { it.botId }.toSet() shouldBe setOf(1, 3, 4)
    }
})
