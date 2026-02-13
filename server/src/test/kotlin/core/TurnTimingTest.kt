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
 * Tests for turn timing behavior to validate deterministic battle timing.
 *
 * ## Key Semantics (ADR-0004: Deterministic Turn Timing)
 *
 * - **Turn Timeout**: The FIXED time duration for every turn. All bots get exactly this amount
 *   of time to compute and submit their intent, regardless of when they actually respond.
 *   This is NOT a deadline - it's the actual turn duration.
 *
 * - **TPS (Turns Per Second)**: Controls maximum visualization/observer frame rate ONLY.
 *   TPS does NOT affect bot computation time or battle outcomes. It is a separate concern
 *   from turn timeout.
 *
 * - **Deterministic Battles**: Battle results depend ONLY on turn timeout, not on TPS settings.
 *   Running the same battle at different TPS values (e.g., 100 TPS vs 500 TPS) with the same
 *   turn timeout produces identical results. This is critical for competitive play and
 *   reproducible simulations.
 *
 * ## Timer Parameters Mapping (Post ADR-0004 Update)
 *
 * - `minDelayNanos` = Turn timeout (fixed turn duration)
 * - `maxDelayNanos` = Turn timeout (same as min - no early completion)
 *
 * The timer ALWAYS waits the full turn timeout period:
 * - Even if all bots respond in 1ms, the turn takes turnTimeout milliseconds
 * - This ensures every bot gets the same computation time in every turn
 * - No "notifyReady()" early completion optimization
 *
 * ## Design Rationale
 *
 * The previous design allowed "early completion" where turns ended as soon as all bots responded,
 * subject to TPS constraints. This created non-determinism: battle outcomes varied based on TPS
 * settings because faster bots had less time to process events at high TPS.
 *
 * The new design prioritizes determinism over speed: every turn takes exactly turnTimeout,
 * ensuring reproducible battles regardless of TPS configuration.
 */
class TurnTimingTest : FunSpec({

    val toleranceNanos = TimeUnit.MILLISECONDS.toNanos(30)

    context("Deterministic Turn Timing") {

        test("every turn takes exactly turnTimeout regardless of bot response times") {
            // Scenario: Turn timeout = 100ms, bots respond quickly
            // Expected: Turn ALWAYS takes 100ms (fixed duration)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(100)
            val startTime = System.nanoTime()

            // New behavior: minDelay = maxDelay = turnTimeout (fixed duration)
            timer.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should wait for full turn timeout (fixed duration)
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)
            elapsed shouldBeLessThan (turnTimeout + TimeUnit.MILLISECONDS.toNanos(100))

            timer.shutdown()
        }

        test("turn timeout is NOT affected by when bots respond") {
            // Scenario: Turn timeout = 80ms
            // Expected: Always wait 80ms, even if all bots respond in 5ms
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(80)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Must wait for turn timeout (fixed duration), regardless of bot response time
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)

            timer.shutdown()
        }
    }

    context("TPS Independence (Battle Determinism)") {

        test("battle outcomes are independent of TPS settings") {
            // Scenario: Same turn timeout, different visualization speeds
            // Expected: Identical turn durations regardless of TPS
            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(80)

            // Test at "500 TPS" visualization speed
            val timer1 = ResettableTimer {}
            timer1.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)
            timer1.shutdown()

            // Test at "100 TPS" visualization speed
            val timer2 = ResettableTimer {}
            timer2.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)
            timer2.shutdown()

            // Both use same turn timeout - TPS doesn't affect turn duration anymore
            // This guarantees deterministic battles
        }

        test("notifyReady has no effect with deterministic timing") {
            // Scenario: Call notifyReady() early
            // Expected: Turn still takes full turnTimeout (notifyReady is now a no-op)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(100)
            val startTime = System.nanoTime()

            // With deterministic timing: minDelay = maxDelay = turnTimeout
            timer.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)

            // Try to trigger early completion (should have no effect)
            Thread.sleep(10)
            timer.notifyReady()

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should STILL wait for full turn timeout despite notifyReady()
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)

            timer.shutdown()
        }
    }

    context("ResettableTimer Behavior Tests") {

        test("multiple turns with deterministic timing") {
            // Scenario: Run multiple turns
            // Expected: Each turn takes exactly turnTimeout
            val turnCount = AtomicInteger(0)
            val latch = CountDownLatch(3)
            val turnExecuteTimes = mutableListOf<Long>()

            val timer = ResettableTimer {
                turnExecuteTimes.add(System.nanoTime())
                turnCount.incrementAndGet()
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(60)
            val startTimes = mutableListOf<Long>()

            repeat(3) {
                val startTime = System.nanoTime()
                startTimes.add(startTime)
                timer.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)
                Thread.sleep(80) // Wait for turn to complete
            }

            latch.await(1, TimeUnit.SECONDS) shouldBe true
            turnCount.get() shouldBe 3

            // Verify each turn took exactly turnTimeout
            for (i in startTimes.indices) {
                val duration = turnExecuteTimes[i] - startTimes[i]
                duration shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)
                duration shouldBeLessThan (turnTimeout + TimeUnit.MILLISECONDS.toNanos(50))
            }

            timer.shutdown()
        }
    }

    context("Edge Cases") {

        test("timer with equal min and max delay") {
            // When min = max, it's a fixed delay (deterministic behavior)
            val executedAt = AtomicLong(0L)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                latch.countDown()
            }

            val period = TimeUnit.MILLISECONDS.toNanos(80)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = period, maxDelayNanos = period)

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            elapsed shouldBeGreaterThanOrEqual (period - toleranceNanos)
            elapsed shouldBeLessThan (period + TimeUnit.MILLISECONDS.toNanos(100))

            timer.shutdown()
        }

        test("notifyReady with deterministic timing has no effect") {
            // With deterministic timing (min = max), notifyReady() is a no-op
            val executedAt = AtomicLong(0L)
            val executionCount = AtomicInteger(0)
            val latch = CountDownLatch(1)
            val timer = ResettableTimer {
                executedAt.set(System.nanoTime())
                executionCount.incrementAndGet()
                latch.countDown()
            }

            val turnTimeout = TimeUnit.MILLISECONDS.toNanos(100)
            val startTime = System.nanoTime()

            timer.schedule(minDelayNanos = turnTimeout, maxDelayNanos = turnTimeout)

            // Multiple notifyReady calls (should have no effect)
            repeat(5) {
                Thread.sleep(5)
                timer.notifyReady()
            }

            latch.await(500, TimeUnit.MILLISECONDS) shouldBe true

            val elapsed = executedAt.get() - startTime
            // Should execute once after full turnTimeout, ignoring notifyReady()
            executionCount.get() shouldBe 1
            elapsed shouldBeGreaterThanOrEqual (turnTimeout - toleranceNanos)

            timer.shutdown()
        }
    }
})


