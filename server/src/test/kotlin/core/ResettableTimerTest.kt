package core

import dev.robocode.tankroyale.server.core.ResettableTimer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference


class ResettableTimerTest : FunSpec({

    val toleranceNanos = TimeUnit.MILLISECONDS.toNanos(25)

    test("executes after max delay when not notified") {
        val executedAt = AtomicLong(0L)
        val latch = CountDownLatch(1)
        val timer = ResettableTimer {
            executedAt.set(System.nanoTime())
            latch.countDown()
        }

        val maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(120)
        val startTime = System.nanoTime()

        timer.schedule(minDelayNanos = 0L, maxDelayNanos = maxDelayNanos)

        latch.await(800, TimeUnit.MILLISECONDS) shouldBe true

        val elapsed = executedAt.get() - startTime
        elapsed shouldBeGreaterThanOrEqual (maxDelayNanos - toleranceNanos)
        elapsed shouldBeLessThan (maxDelayNanos + TimeUnit.MILLISECONDS.toNanos(250))

        timer.shutdown()
    }

    test("notifyReady before min delay waits for min delay") {
        val executedAt = AtomicLong(0L)
        val latch = CountDownLatch(1)
        val timer = ResettableTimer {
            executedAt.set(System.nanoTime())
            latch.countDown()
        }

        val minDelayNanos = TimeUnit.MILLISECONDS.toNanos(140)
        val maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(400)
        val startTime = System.nanoTime()

        timer.schedule(minDelayNanos = minDelayNanos, maxDelayNanos = maxDelayNanos)
        timer.notifyReady()

        latch.await(900, TimeUnit.MILLISECONDS) shouldBe true

        val elapsed = executedAt.get() - startTime
        elapsed shouldBeGreaterThanOrEqual (minDelayNanos - toleranceNanos)
        elapsed shouldBeLessThan (maxDelayNanos + TimeUnit.MILLISECONDS.toNanos(250))

        timer.shutdown()
    }

    test("notifyReady after min delay executes promptly") {
        val executedAt = AtomicLong(0L)
        val latch = CountDownLatch(1)
        val timer = ResettableTimer {
            executedAt.set(System.nanoTime())
            latch.countDown()
        }

        val minDelayNanos = TimeUnit.MILLISECONDS.toNanos(60)
        val maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(500)
        val startTime = System.nanoTime()

        timer.schedule(minDelayNanos = minDelayNanos, maxDelayNanos = maxDelayNanos)
        Thread.sleep(90)
        val notifyTime = System.nanoTime()
        timer.notifyReady()

        latch.await(700, TimeUnit.MILLISECONDS) shouldBe true

        val elapsed = executedAt.get() - startTime
        val notifyElapsed = executedAt.get() - notifyTime
        elapsed shouldBeGreaterThanOrEqual (minDelayNanos - toleranceNanos)
        notifyElapsed shouldBeLessThan (TimeUnit.MILLISECONDS.toNanos(140))

        timer.shutdown()
    }

    test("cancel prevents execution") {
        val latch = CountDownLatch(1)
        val timer = ResettableTimer { latch.countDown() }

        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(120))
        timer.cancel()

        latch.await(250, TimeUnit.MILLISECONDS) shouldBe false

        timer.shutdown()
    }

    test("pause and resume excludes paused time") {
        val executedAt = AtomicLong(0L)
        val latch = CountDownLatch(1)
        val timer = ResettableTimer {
            executedAt.set(System.nanoTime())
            latch.countDown()
        }

        val maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(200)
        val pauseMillis = 140L
        val startTime = System.nanoTime()

        timer.schedule(minDelayNanos = 0L, maxDelayNanos = maxDelayNanos)
        Thread.sleep(60)
        timer.pause()

        latch.await(150, TimeUnit.MILLISECONDS) shouldBe false

        Thread.sleep(pauseMillis)
        timer.resume()

        latch.await(600, TimeUnit.MILLISECONDS) shouldBe true

        val elapsed = executedAt.get() - startTime
        val expectedMinElapsed = maxDelayNanos + TimeUnit.MILLISECONDS.toNanos(pauseMillis) - toleranceNanos

        elapsed shouldBeGreaterThanOrEqual expectedMinElapsed
        elapsed shouldBeLessThan (maxDelayNanos + TimeUnit.MILLISECONDS.toNanos(pauseMillis + 250))

        timer.shutdown()
    }

    test("shutdown stops execution") {
        val latch = CountDownLatch(1)
        val timer = ResettableTimer { latch.countDown() }

        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(200))
        timer.shutdown()

        latch.await(300, TimeUnit.MILLISECONDS) shouldBe false
    }

    test("reuses a single timer thread across many schedules") {
        val latchRef = AtomicReference(CountDownLatch(0))
        val timer = ResettableTimer { latchRef.get().countDown() }

        val firstLatch = CountDownLatch(1)
        latchRef.set(firstLatch)
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = 0L)
        firstLatch.await(1, TimeUnit.SECONDS) shouldBe true

        val baselineCount = countTimerThreads()

        repeat(9_999) {
            val latch = CountDownLatch(1)
            latchRef.set(latch)
            timer.schedule(minDelayNanos = 0L, maxDelayNanos = 0L)
            latch.await(1, TimeUnit.SECONDS) shouldBe true
        }

        val afterCount = countTimerThreads()
        afterCount shouldBe baselineCount

        timer.shutdown()
    }

    test("executes immediately when min delay is 0 and notifyReady called (high TPS scenario)") {
        val executedAt = AtomicLong(0L)
        val latch = CountDownLatch(1)
        val timer = ResettableTimer {
            executedAt.set(System.nanoTime())
            latch.countDown()
        }

        val startTime = System.nanoTime()

        // This simulates TPS=-1 where minDelayNanos is 0
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))

        // Sleep a tiny bit to ensure some time has elapsed
        Thread.sleep(1)

        // When ready is notified and minDelay has passed, should execute immediately
        timer.notifyReady()

        // Should complete immediately, not wait for the 1-second max delay
        latch.await(100, TimeUnit.MILLISECONDS) shouldBe true

        val elapsed = executedAt.get() - startTime
        // Should execute quickly (within 50ms), not wait for 1 second
        elapsed shouldBeLessThan TimeUnit.MILLISECONDS.toNanos(50)

        timer.shutdown()
    }

    test("executes on executor thread not calling thread when delay is 0 (prevents re-entrancy)") {
        val executorThreadName = AtomicReference<String>()
        val latch = CountDownLatch(1)
        val timer = ResettableTimer {
            executorThreadName.set(Thread.currentThread().name)
            latch.countDown()
        }

        val callingThreadName = Thread.currentThread().name

        // Schedule with 0 delay - should execute on executor thread, not calling thread
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))
        timer.notifyReady()

        latch.await(100, TimeUnit.MILLISECONDS) shouldBe true

        // Verify execution happened on the executor thread, not the calling thread
        executorThreadName.get() shouldBe "TurnTimeoutTimer"
        (executorThreadName.get() == callingThreadName) shouldBe false

        timer.shutdown()
    }

    test("rapid schedule with 0 delay does not queue tasks (TPS=-1 simulation)") {
        val executionCount = java.util.concurrent.atomic.AtomicInteger(0)
        val timer = ResettableTimer {
            executionCount.incrementAndGet()
            // Simulate some work
            Thread.sleep(1)
        }

        val startTime = System.nanoTime()
        val iterations = 100

        // Simulate rapid turn updates at TPS=-1
        repeat(iterations) {
            timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))
            timer.notifyReady()
        }

        val elapsed = System.nanoTime() - startTime

        // With immediate execution, this should complete quickly
        // With queueing, this would take much longer
        elapsed shouldBeLessThan TimeUnit.MILLISECONDS.toNanos(500)

        timer.shutdown()
    }

    test("simulates game loop at TPS=-1 without re-entrancy") {
        val executionThreads = mutableListOf<String>()
        val latch = CountDownLatch(10)
        var turnCounter = 0

        val timer = ResettableTimer {
            executionThreads.add(Thread.currentThread().name)
            turnCounter++
            latch.countDown()
        }

        // Simulate 10 turns at TPS=-1
        repeat(10) {
            timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(30))
            timer.notifyReady()
            // Small delay to let executor thread process
            Thread.sleep(2)
        }

        latch.await(1, TimeUnit.SECONDS) shouldBe true

        // All executions should be on the executor thread
        executionThreads.forEach { threadName ->
            threadName shouldBe "TurnTimeoutTimer"
        }

        // Should complete all 10 turns
        turnCounter shouldBe 10

        timer.shutdown()
    }
})

private fun countTimerThreads(): Int =
    Thread.getAllStackTraces().keys.count { it.isAlive && it.name == "TurnTimeoutTimer" }






