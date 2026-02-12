package core

import dev.robocode.tankroyale.server.core.ResettableTimer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ConcurrentHashMap


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

    // ========================================
    // Bug Detection Tests
    // ========================================
    // These tests detect specific bugs in v0.35.3 and v0.35.4
    // - v0.35.3: executor.execute() doesn't set scheduledFuture, so cancelScheduled() can't cancel queued tasks
    // - v0.35.4: executeIfValid() directly → re-entrancy
    // - v0.35.5: executor.submit() returns Future that IS assigned to scheduledFuture → can cancel
    // ========================================

    /**
     * Detects v0.35.3 bug: executor.execute() doesn't set scheduledFuture, so cancel() fails.
     *
     * In v0.35.3, when delay=0:
     *   executor.execute { executeIfValid(generationId) }  // scheduledFuture NOT set!
     *
     * So when timer.cancel() is called, scheduledFuture is null and cancelScheduled() does nothing.
     * The task still runs because it's already in the executor queue.
     *
     * v0.35.5 fix: scheduledFuture = executor.submit { ... } - Future IS stored, cancel works
     *
     * Test strategy:
     * 1. Schedule a blocking job (job1) to occupy the executor
     * 2. While job1 is running, schedule another job (job2) with delay=0
     * 3. Job2 gets queued behind job1
     * 4. Call cancel() - this should cancel job2
     * 5. Let job1 finish
     * 6. Check if job2 ran (it shouldn't in v0.35.5, but will in v0.35.3)
     */
    test("v0.35.3 bug: cancel must prevent queued task from running") {
        val job1Started = CountDownLatch(1)
        val job1CanFinish = CountDownLatch(1)
        val job2Ran = AtomicBoolean(false)
        val jobId = AtomicInteger(0)

        val timer = ResettableTimer {
            val id = jobId.incrementAndGet()
            if (id == 1) {
                // Job 1: block
                job1Started.countDown()
                job1CanFinish.await(5, TimeUnit.SECONDS)
            } else {
                // Job 2+: should not run if cancel worked
                job2Ran.set(true)
            }
        }

        // Schedule job 1 - it will block the executor
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(10))
        timer.notifyReady()

        // Wait for job 1 to start blocking
        job1Started.await(1, TimeUnit.SECONDS)

        // Schedule job 2 while executor is blocked - it gets queued
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.MILLISECONDS.toNanos(10))
        timer.notifyReady()

        // Cancel job 2 before it can run
        // v0.35.3: scheduledFuture is null, cancel does nothing, job2 will run
        // v0.35.5: scheduledFuture is set, cancel removes job2 from queue
        timer.cancel()

        // Let job 1 finish
        job1CanFinish.countDown()

        // Wait for any queued tasks
        Thread.sleep(200)
        timer.shutdown()

        // v0.35.3: job2Ran is TRUE - cancel couldn't stop it (scheduledFuture was null)
        // v0.35.5: job2Ran is FALSE - cancel worked (Future.cancel())
        job2Ran.get() shouldBe false
    }

    test("v0.35.4 bug: prevents re-entrancy when delay is 0 (TPS=-1 scenario)") {
        val callingThreadName = Thread.currentThread().name
        val executionThreadName = AtomicReference<String>()
        val reentrantCallDetected = AtomicBoolean(false)
        val recursionDepth = ThreadLocal.withInitial { AtomicInteger(0) }
        val latch = CountDownLatch(1)
        lateinit var timer: ResettableTimer

        timer = ResettableTimer {
            val depth = recursionDepth.get().incrementAndGet()
            executionThreadName.set(Thread.currentThread().name)

            // v0.35.4 BUG: Direct execution causes recursion here
            if (depth > 1) {
                reentrantCallDetected.set(true)
            }

            try {
                // Simulate onNextTurn() calling resetTurnTimeout()
                if (depth == 1) {
                    timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))
                    timer.notifyReady()
                }
                latch.countDown()
            } finally {
                recursionDepth.get().decrementAndGet()
            }
        }

        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))
        timer.notifyReady()

        latch.await(2, TimeUnit.SECONDS)
        timer.shutdown()

        val execThread = executionThreadName.get()

        // PASS on v0.35.5: No re-entrancy, executes on TurnTimeoutTimer thread
        // FAIL on v0.35.4: Re-entrancy detected, executes on calling thread
        // PASS on v0.35.3: No re-entrancy (but has queueing issue)
        reentrantCallDetected.get() shouldBe false
        execThread shouldBe "TurnTimeoutTimer"
        (execThread == callingThreadName) shouldBe false
    }


    test("TPS=-1 game simulation: 100 turns without re-entrancy or queueing") {
        val turnsToSimulate = 100
        val turnsExecuted = AtomicInteger(0)
        val executionThreads = ConcurrentHashMap.newKeySet<String>()
        val reentrantCallDetected = AtomicBoolean(false)
        val recursionDepth = ThreadLocal.withInitial { AtomicInteger(0) }

        lateinit var timer: ResettableTimer

        timer = ResettableTimer {
            val depth = recursionDepth.get().incrementAndGet()
            executionThreads.add(Thread.currentThread().name)

            if (depth > 1) {
                reentrantCallDetected.set(true)
            }

            try {
                val turnNum = turnsExecuted.incrementAndGet()
                Thread.sleep(1) // Simulate game logic

                // Schedule next turn (like real game does)
                if (turnNum < turnsToSimulate) {
                    timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(30))
                    timer.notifyReady()
                }
            } finally {
                recursionDepth.get().decrementAndGet()
            }
        }

        // Start the game loop
        val startTime = System.currentTimeMillis()
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(30))
        timer.notifyReady()

        // Wait for completion
        var waited = 0
        while (turnsExecuted.get() < turnsToSimulate && waited < 10000) {
            Thread.sleep(10)
            waited += 10
        }

        timer.shutdown()
        val totalTimeMs = System.currentTimeMillis() - startTime

        // PASS on v0.35.5: All 100 turns execute, no re-entrancy, fast (~100-500ms)
        // FAIL on v0.35.4: Re-entrancy detected
        // FAIL on v0.35.3: May timeout or be very slow due to queueing
        turnsExecuted.get() shouldBe turnsToSimulate
        reentrantCallDetected.get() shouldBe false
        executionThreads.size shouldBe 1
        executionThreads.first() shouldBe "TurnTimeoutTimer"
        (totalTimeMs < 5000) shouldBe true // Should complete fast
    }
})

private fun countTimerThreads(): Int =
    Thread.getAllStackTraces().keys.count { it.isAlive && it.name == "TurnTimeoutTimer" }
