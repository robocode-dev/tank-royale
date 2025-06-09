package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.common.Event
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class EventTest : FunSpec({

    context("Event class") {

        test("enqueue") {
            val event = Event<Unit>()
            val counter = AtomicInteger(0)
            val latch = CountDownLatch(1)

            event.enqueue(this) {
                counter.incrementAndGet()
                latch.countDown()
            }
            event.fire(Unit)

            // Wait for the EDT to process the event
            withContext(Dispatchers.Default) {
                latch.await(5, TimeUnit.SECONDS) shouldBe true
            }

            counter.get() shouldBe 1
        }
    }
})
