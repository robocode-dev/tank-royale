package core

import dev.robocode.tankroyale.server.core.ResettableTimer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Tests for turn timing behavior to validate the relationship between Turn Timeout and TPS.
 *
 * ## Key Semantics (see ADR-0004)
 *
 * - **Turn Timeout**: The maximum time bots have to submit their intent before being skipped.
 *   This is the "deadline" - bots that respond after this skip the turn.
 *
 * - **TPS (Turns Per Second)**: Controls visualization speed. The minimum time between turns
 *   is `1/TPS` seconds. This prevents the game from running faster than desired for spectators.
 *
 * - **Early Completion**: If all bots submit intents before the turn timeout, the server
 *   CAN proceed early (subject to TPS constraint). This enables fast batch simulations.
 *
 * ## Timer Parameters Mapping
 *
 * - `minDelayNanos` = TPS period (`1_000_000_000 / TPS`) or 0 if TPS <= 0
 * - `maxDelayNanos` = Turn timeout (the deadline for bot responses)
 *
 * When `notifyReady()` is called (all bots responded):
 * - If min delay hasn't elapsed: wait until min delay (TPS constraint)
 * - If min delay has elapsed: proceed immediately
 *
 * Without `notifyReady()` (not all bots responded):
 * - Wait until max delay (turn timeout deadline), then proceed (late bots skip)
 */
class TurnTimingTest : FunSpec({

    val toleranceNanos = TimeUnit.MILLISECONDS.toNanos(30)

    context("Turn Timeout Guarantees") {

        test("bots get full turn timeout when NOT all respond - this is the deadline") {
            // Scenario: 2 of 3 bots respond, 1 is slow
            // Expected: Server waits until turn timeout (the deadline)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(100)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(20) // Fast TPS, but timeout is longer
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)
            // Simulate: NOT calling notifyReady() - some bot didn't respond

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should wait for full turn timeout (the deadline)
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)
            elapsed shouldBeLessThan (turnTimeout + TimeUnit.MILLISECONDS.toNanos(100))

            timer.shutdown()
        }

        test("turn timeout is respected even when TPS would allow faster execution") {
            // Scenario: TPS=100 (10ms period), but turn timeout is 50ms
            // Without notifyReady, should wait 50ms (the deadline)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(80)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(10) // Very fast TPS
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)
            // NOT calling notifyReady - simulates missing bot response

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Must wait for turn timeout (deadline), not just TPS period
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)

            timer.shutdown()
        }
    }

    context("Early Completion (All Bots Respond)") {

        test("when all bots respond and TPS allows, proceed at TPS rate") {
            // Scenario: Turn timeout = 100ms, TPS period = 30ms, all bots respond in 5ms
            // Expected: Wait until TPS period (30ms), then proceed
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(150)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(50)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Simulate all bots responding quickly (5ms)
            Thread.sleep(5)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should wait for TPS period (visualization constraint), but NOT full turn timeout
            elapsed shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)
            elapsed shouldBeLessThan (turnTimeout - TimeUnit.MILLISECONDS.toNanos(20))

            timer.shutdown()
        }

        test("when all bots respond AFTER TPS period, proceed immediately") {
            // Scenario: Turn timeout = 100ms, TPS period = 20ms, all bots respond at 40ms
            // Expected: Proceed immediately (TPS period already elapsed)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(150)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(30)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Wait past TPS period before all bots respond
            Thread.sleep(60)
            val notifyTime = System.nanoTime()
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            val notifyToExecution = executedAt.get() - notifyTime

            // Should have proceeded shortly after notifyReady (TPS period already passed)
            elapsed shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)
            notifyToExecution shouldBeLessThan TimeUnit.MILLISECONDS.toNanos(50)
            // Should NOT have waited for full turn timeout
            elapsed shouldBeLessThan (turnTimeout - TimeUnit.MILLISECONDS.toNanos(20))

            timer.shutdown()
        }

        test("unlimited TPS (TPS=-1) with early response proceeds immediately") {
            // Scenario: TPS=-1 means minDelay=0, turn timeout=100ms, bots respond in 5ms
            // Expected: Proceed immediately (no TPS constraint)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(150)
            val tpsPeriod = 0L // TPS=-1 means unlimited
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Simulate quick bot response
            Thread.sleep(5)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should proceed very quickly (no TPS delay)
            elapsed shouldBeLessThan TimeUnit.MILLISECONDS.toNanos(80)
            // Should NOT wait for turn timeout
            elapsed shouldBeLessThan (turnTimeout - TimeUnit.MILLISECONDS.toNanos(50))

            timer.shutdown()
        }

        test("unlimited TPS without response still waits for turn timeout") {
            // Scenario: TPS=-1, turn timeout=80ms, but NOT all bots respond
            // Expected: Wait for turn timeout (deadline applies)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(80)
            val tpsPeriod = 0L // TPS=-1 means unlimited
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)
            // NOT calling notifyReady - simulates missing bot response

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Must wait for turn timeout (deadline)
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)

            timer.shutdown()
        }
    }

    context("TPS Constraint") {

        test("TPS limits maximum turn rate even when bots respond instantly") {
            // Scenario: Run multiple turns with instant bot responses
            // Expected: Each turn takes at least TPS period
            val turnCount = AtomicInteger(0)
            val latch = CountDownLatch(5)
            val turnTimes = mutableListOf<Long>()

            val timer = ResettableTimer {
                turnTimes.add(System.nanoTime())
                turnCount.incrementAndGet()
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(200)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(40) // 25 TPS

            val startTime = System.nanoTime()

            repeat(5) {
                timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)
                timer.notifyReady() // Bots respond instantly
                // Wait for this turn to complete before scheduling next
                Thread.sleep(60)
            }

            latch.await(2, TimeUnit.SECONDS) shouldBe true
            turnCount.get() shouldBe 5

            // Verify each turn took at least TPS period
            for (i in 1 until turnTimes.size) {
                val turnDuration = turnTimes[i] - turnTimes[i - 1]
                turnDuration shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)
            }

            timer.shutdown()
        }

        test("low TPS (slow playback) is respected even with fast bots") {
            // Scenario: TPS=5 (200ms period), bots respond in 10ms
            // Expected: Turn takes ~200ms (TPS constraint dominates)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(300)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(150) // Very slow TPS
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Bots respond quickly
            Thread.sleep(10)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should wait for TPS period (visualization speed)
            elapsed shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)
            // But not for turn timeout (bots responded)
            elapsed shouldBeLessThan (turnTimeout - TimeUnit.MILLISECONDS.toNanos(50))

            timer.shutdown()
        }
    }

    context("Edge Cases") {

        test("TPS period equals turn timeout - both are honored") {
            // When TPS period = turn timeout, they're effectively the same constraint
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val period = TimeUnit.MILLISECONDS.toNanos(80)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = period, maxDelayNanos = period)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            elapsed shouldBeGreaterThanOrEqual (period - toleranceNanos)
            elapsed shouldBeLessThan (period + TimeUnit.MILLISECONDS.toNanos(100))

            timer.shutdown()
        }

        test("TPS period > turn timeout - TPS dominates (edge case configuration)") {
            // This is an unusual config where TPS is slower than turn timeout allows
            // The implementation uses max(minDelay, maxDelay) for safety
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(40)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(100) // TPS slower than timeout allows

            // GameServer uses maxOf(minPeriod, maxPeriod) for maxDelayNanos
            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = maxOf(tpsPeriod, turnTimeout))
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // TPS period should be respected
            elapsed shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)

            timer.shutdown()
        }

        test("notifyReady called multiple times - only first matters") {
            val executedAt = AtomicLong(0L)
            val executionCount = AtomicInteger(0)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                executionCount.incrementAndGet()
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(150)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(50)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Multiple notifyReady calls (simulates multiple bots responding)
            repeat(5) {
                Thread.sleep(5)
                timer.notifyReady()
            }

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should execute once, respecting TPS period
            executionCount.get() shouldBe 1
            elapsed shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)
            elapsed shouldBeLessThan (turnTimeout - TimeUnit.MILLISECONDS.toNanos(20))

            timer.shutdown()
        }
    }
})


