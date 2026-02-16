package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.common.Event
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
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

        test("atomic operations handle concurrent access") {
            val event = Event<Int>()
            val counter = AtomicInteger(0)
            val fixedOwner = Any()

            event.subscribe(fixedOwner) {
                counter.incrementAndGet()
            }

            val fireThreads = 4
            val firesPerThread = 200
            val updateThreads = 2
            val executor = Executors.newFixedThreadPool(fireThreads + updateThreads)
            val startLatch = CountDownLatch(1)
            val doneLatch = CountDownLatch(fireThreads + updateThreads)

            try {
                repeat(fireThreads) {
                    executor.execute {
                        startLatch.await()
                        repeat(firesPerThread) { iteration ->
                            event.fire(iteration)
                        }
                        doneLatch.countDown()
                    }
                }

                repeat(updateThreads) {
                    executor.execute {
                        startLatch.await()
                        repeat(200) {
                            val owner = Any()
                            event.subscribe(owner) { }
                            event -= owner
                        }
                        doneLatch.countDown()
                    }
                }

                startLatch.countDown()
                doneLatch.await(10, TimeUnit.SECONDS) shouldBe true
            } finally {
                executor.shutdownNow()
                executor.awaitTermination(5, TimeUnit.SECONDS)
            }

            counter.get() shouldBe fireThreads * firesPerThread
        }

        test("weak references are released") {
            val event = Event<Unit>()
            val counter = AtomicInteger(0)
            var owner: Any? = Any()
            val weakOwner = WeakReference(owner)

            event.subscribe(owner!!) {
                counter.incrementAndGet()
            }

            owner = null

            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (weakOwner.get() != null && System.nanoTime() < deadline) {
                System.gc()
                Thread.sleep(50)
            }

            weakOwner.get() shouldBe null

            event.fire(Unit)
            counter.get() shouldBe 0
        }
    }
})
