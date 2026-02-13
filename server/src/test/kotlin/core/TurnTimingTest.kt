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

        test("when all bots respond before turn timeout, proceed at TPS rate (not wait for turn timeout)") {
            // CORRECT BEHAVIOR: If all bots respond, we can proceed as fast as TPS allows
            // Scenario: Turn timeout = 150ms, TPS period = 50ms, all bots respond in 5ms
            // Expected: Wait for TPS period (50ms), NOT turn timeout (150ms)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(150)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(50)
            val startTime = System.nanoTime()

            // GameServer uses: minDelay=tpsPeriod, maxDelay=turnTimeout
            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Simulate all bots responding quickly (5ms)
            Thread.sleep(5)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should wait for TPS period (50ms), not turn timeout (150ms)
            elapsed shouldBeGreaterThanOrEqual (tpsPeriod - toleranceNanos)
            elapsed shouldBeLessThan (turnTimeout - toleranceNanos)

            timer.shutdown()
        }

        test("when TPS period > turn timeout, use TPS period (bots get more time)") {
            // ...existing code...
            // Scenario: Turn timeout = 50ms, TPS period = 80ms (slow visualization)
            // When TPS is slower than timeout, TPS controls (bots get MORE time)
            // Expected: Wait 80ms
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(50)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(80) // Slower TPS means more time
            val startTime = System.nanoTime()

            val effectiveDelay = maxOf(turnTimeout, tpsPeriod)
            timer.schedule(minDelayNanos = effectiveDelay, maxDelayNanos = effectiveDelay)

            // Wait past turn timeout but before TPS period
            Thread.sleep(60)
            val notifyTime = System.nanoTime()
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            val notifyToExecution = executedAt.get() - notifyTime

            // Should wait for full effective delay (TPS period in this case)
            elapsed shouldBeGreaterThanOrEqual (effectiveDelay - toleranceNanos)
            // notifyToExecution should be small (just the remaining time after notify)
            notifyToExecution shouldBeLessThan TimeUnit.MILLISECONDS.toNanos(50)

            timer.shutdown()
        }

        test("unlimited TPS (TPS=-1) with all bots responding proceeds immediately") {
            // CORRECT BEHAVIOR: At TPS=-1 with all bots responding, proceed immediately
            // Scenario: TPS=-1 (tpsPeriod=0), turn timeout=100ms, bots respond in 5ms
            // Expected: Proceed immediately (~5ms), not wait for turn timeout
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(100)
            val tpsPeriod = 0L // TPS=-1 means unlimited
            val startTime = System.nanoTime()

            // GameServer uses: minDelay=0 (TPS period), maxDelay=turnTimeout
            timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)

            // Simulate quick bot response
            Thread.sleep(5)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should proceed immediately (no TPS constraint), not wait for turn timeout
            elapsed shouldBeLessThan (turnTimeout - toleranceNanos)

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
            val turnStartTimes = mutableListOf<Long>()
            val turnExecuteTimes = mutableListOf<Long>()

            val timer = ResettableTimer {
                turnExecuteTimes.add(System.nanoTime())
                turnCount.incrementAndGet()
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(200)
            val tpsPeriod = TimeUnit.MILLISECONDS.toNanos(40) // 25 TPS

            repeat(5) {
                val turnStartTime = System.nanoTime()
                turnStartTimes.add(turnStartTime)
                timer.schedule(minDelayNanos = tpsPeriod, maxDelayNanos = turnTimeout)
                timer.notifyReady() // Bots respond instantly
                // Wait for this turn to complete before scheduling next
                Thread.sleep(60)
            }

            latch.await(2, TimeUnit.SECONDS) shouldBe true
            turnCount.get() shouldBe 5

            // Verify each turn took at least TPS period (from start to execution)
            for (i in 0 until turnStartTimes.size) {
                val turnDuration = turnExecuteTimes[i] - turnStartTimes[i]
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
            val startTime = System.nanoTime()

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


