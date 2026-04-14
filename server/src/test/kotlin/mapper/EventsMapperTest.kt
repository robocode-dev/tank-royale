@file:OptIn(io.kotest.common.ExperimentalKotest::class)

package mapper

import dev.robocode.tankroyale.server.event.BotDeathEvent
import dev.robocode.tankroyale.server.mapper.EventsMapper
import dev.robocode.tankroyale.server.model.BotId
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class EventsMapperTest : FunSpec({

    context("TR-SRV-MAP-001: Events mapping").config(tags = setOf(Tag("TR-SRV-MAP-001"))) {

        test("Positive: Valid BotDeathEvent mapping") {
            val serverEvent = BotDeathEvent(42, BotId(7))
            val mappedEvents = EventsMapper.map(setOf(serverEvent))

            mappedEvents.size shouldBe 1
            val mappedEvent = mappedEvents[0]
            mappedEvent.shouldBeInstanceOf<dev.robocode.tankroyale.schema.BotDeathEvent>()
            mappedEvent.apply {
                turnNumber shouldBe 42
                victimId shouldBe 7
            }
        }

        test("Negative: Unsupported event type (theoretical)") {
            // Since Event is a sealed class, we can't create an unsupported event type
            // without it being a compilation error in the mapper itself.
            // This test is here to represent the requirement, but in practice,
            // the Kotlin compiler handles the "unknown" case by enforcing exhaustiveness.
        }
    }
})
