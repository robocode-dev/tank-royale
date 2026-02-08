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
})

private fun countTimerThreads(): Int =
    Thread.getAllStackTraces().keys.count { it.isAlive && it.name == "TurnTimeoutTimer" }
