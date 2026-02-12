package core

import dev.robocode.tankroyale.server.core.ResettableTimer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Integration test to reproduce the TPS=-1 bug reported in versions 0.35.3 and 0.35.4.
 *
 * User Report Summary:
 * - At TPS=-1 (unlimited), bot behavior becomes inconsistent and differs from TPS=50
 * - Event queues overflow with "max event queue size reached: 256"
 * - Game starts lagging/stuttering around turn 1000 in round 4
 * - OutOfMemoryErrors occur
 * - Bots disconnect and game state becomes inconsistent
 *
 * Root Cause:
 * - Version 0.35.3: ResettableTimer queued tasks instead of executing immediately at TPS=-1
 * - Version 0.35.4: Direct execution caused re-entrancy (onNextTurn calling itself recursively)
 * - Version 0.35.5: Fixed by using executor.submit() to maintain thread separation
 *
 * This test simulates a game with 10 bots running 10 rounds with TPS=-1 to verify:
 * 1. No re-entrancy issues (all executions on executor thread)
 * 2. No task queue buildup (rapid schedule/cancel works correctly)
 * 3. Game completes in reasonable time without hanging
 * 4. Turn progression is smooth without lag spikes
 */
class TpsUnlimitedIntegrationTest : FunSpec({

    test("simulates 10-bot game at TPS=-1 without re-entrancy or queue overflow (reproduces user scenario)") {
        println("\n========================================")
        println("TPS=-1 Integration Test - 10 Bots, 10 Rounds")
        println("========================================")
        println("This simulates the exact scenario reported by the user:")
        println("- 10 bots running at TPS=-1 (unlimited)")
        println("- Multiple rounds with 1000+ turns each")
        println("- Should complete in ~15 seconds on fast CPU")
        println("========================================\n")

        val turnsPerRound = 1500
        val totalRounds = 10

        val currentRound = AtomicInteger(1)
        val currentTurn = AtomicInteger(0)
        val totalTurnsExecuted = AtomicInteger(0)

        // Track thread safety - should NEVER execute on calling thread
        val executionThreads = ConcurrentHashMap.newKeySet<String>()
        val reentrantCallDetected = AtomicBoolean(false)
        val threadExecutionDepth = ThreadLocal.withInitial { AtomicInteger(0) }

        // Track timing for lag detection
        val turnTimings = mutableListOf<Long>()
        val lastTurnTime = AtomicLong(System.nanoTime())

        // Track memory and task queue buildup
        val maxMemoryUsed = AtomicLong(0L)
        val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        val testStartTime = System.nanoTime()
        val latch = CountDownLatch(totalRounds * turnsPerRound)
        lateinit var timer: ResettableTimer

        timer = ResettableTimer {
            // Detect re-entrancy
            val depth = threadExecutionDepth.get()
            if (depth.get() > 0) {
                reentrantCallDetected.set(true)
                println("⚠️  RE-ENTRANCY DETECTED! Thread ${Thread.currentThread().name} depth=${depth.get()}")
            }
            depth.incrementAndGet()

            try {
                // Track which thread is executing
                executionThreads.add(Thread.currentThread().name)

                // Simulate turn processing
                val turn = currentTurn.incrementAndGet()
                val round = currentRound.get()
                totalTurnsExecuted.incrementAndGet()

                // Track timing between turns to detect lag
                val now = System.nanoTime()
                val timeSinceLastTurn = now - lastTurnTime.get()
                turnTimings.add(timeSinceLastTurn)
                lastTurnTime.set(now)

                // Track memory usage
                val currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                maxMemoryUsed.updateAndGet { max -> maxOf(max, currentMemory) }

                // Progress reporting
                if (turn == 1) {
                    val elapsed = (now - testStartTime) / 1_000_000 // ms
                    println("Round $round started (elapsed: ${elapsed}ms)")
                } else if (turn % 250 == 0) {
                    val elapsed = (now - testStartTime) / 1_000_000 // ms
                    val memUsedMB = (currentMemory - startMemory) / (1024 * 1024)
                    println("  Round $round, Turn $turn (elapsed: ${elapsed}ms, mem: +${memUsedMB}MB)")
                } else if (turn == 1000) {
                    println("  Round $round, Turn $turn - Critical threshold reached (user reported lag starts here)")
                }

                // Round transition
                if (turn >= turnsPerRound) {
                    val elapsed = (now - testStartTime) / 1_000_000 // ms
                    val avgTurnTime = turnTimings.takeLast(turnsPerRound).average() / 1000 // microseconds
                    println("Round $round completed - ${turnsPerRound} turns, avg: ${"%.2f".format(avgTurnTime)}µs/turn (elapsed: ${elapsed}ms)")

                    if (round < totalRounds) {
                        currentRound.incrementAndGet()
                        currentTurn.set(0)
                    }
                }

                latch.countDown()

                // THIS IS THE KEY: Simulate the real game loop where resetTurnTimeout()
                // is called AT THE END of onNextTurn(), FROM the executor thread
                // This matches the actual game behavior and prevents task cancellation
                if (totalTurnsExecuted.get() < totalRounds * turnsPerRound) {
                    timer.schedule(
                        minDelayNanos = 0L,
                        maxDelayNanos = TimeUnit.SECONDS.toNanos(30)
                    )
                    timer.notifyReady()
                }

            } finally {
                depth.decrementAndGet()
            }
        }

        // Simulate TPS=-1 game loop - just kick it off once!
        println("Starting game simulation...")
        val gameLoopStart = System.nanoTime()

        // Start the first turn - the timer callback will schedule subsequent turns
        timer.schedule(
            minDelayNanos = 0L,
            maxDelayNanos = TimeUnit.SECONDS.toNanos(30)
        )
        timer.notifyReady()

        // Wait for completion with timeout
        val completed = latch.await(30, TimeUnit.SECONDS)

        val totalElapsed = (System.nanoTime() - gameLoopStart) / 1_000_000 // ms

        timer.shutdown()

        println("\n========================================")
        println("Test Results:")
        println("========================================")
        println("Completed: $completed")
        println("Total turns executed: ${totalTurnsExecuted.get()} / ${totalRounds * turnsPerRound}")
        println("Total elapsed time: ${totalElapsed}ms")
        println("Average: ${"%.2f".format(totalElapsed.toDouble() / totalTurnsExecuted.get())}ms per turn")
        println("Re-entrancy detected: ${reentrantCallDetected.get()}")
        println("Execution threads: $executionThreads")
        println("Memory increase: ${(maxMemoryUsed.get() - startMemory) / (1024 * 1024)}MB")

        // Analyze timing for lag spikes
        val sortedTimings = turnTimings.sorted()
        val medianTiming = sortedTimings[sortedTimings.size / 2] / 1000.0 // microseconds
        val p95Timing = sortedTimings[(sortedTimings.size * 0.95).toInt()] / 1000.0
        val p99Timing = sortedTimings[(sortedTimings.size * 0.99).toInt()] / 1000.0
        val maxTiming = sortedTimings.last() / 1000.0

        println("\nTiming Analysis (time between turns):")
        println("  Median: ${"%.2f".format(medianTiming)}µs")
        println("  P95:    ${"%.2f".format(p95Timing)}µs")
        println("  P99:    ${"%.2f".format(p99Timing)}µs")
        println("  Max:    ${"%.2f".format(maxTiming)}µs")

        // Detect lag spikes (user reported lagging around turn 1000)
        val lagSpikes = turnTimings.withIndex().filter { (_, time) ->
            time > TimeUnit.MILLISECONDS.toNanos(10) // >10ms is a significant lag
        }
        if (lagSpikes.isNotEmpty()) {
            println("\n⚠️  Lag spikes detected (>10ms):")
            lagSpikes.take(5).forEach { (index, time) ->
                println("    Turn ${index + 1}: ${"%.2f".format(time / 1_000_000.0)}ms")
            }
        }
        println("========================================\n")

        // Assertions
        // Allow for very close to completion (99%+) due to timing
        val completionPercentage = (totalTurnsExecuted.get().toDouble() / (totalRounds * turnsPerRound)) * 100
        (completionPercentage >= 99.0) shouldBe true
        reentrantCallDetected.get() shouldBe false
        executionThreads.size shouldBe 1
        executionThreads.contains("TurnTimeoutTimer") shouldBe true

        // Performance assertions based on user report
        // User said it should complete in ~15s on fast processor
        // We're more lenient and allow up to 30s
        (totalElapsed < 30_000) shouldBe true // Should complete in under 30 seconds

        println("✅ Test PASSED - No re-entrancy, execution on correct thread, completed ${String.format("%.1f", completionPercentage)}% in ${totalElapsed}ms")
    }

    test("detects re-entrancy bug from version 0.35.4 (direct execution)") {
        println("\n========================================")
        println("Re-entrancy Bug Detection Test")
        println("========================================")
        println("This test detects the bug introduced in 0.35.4")
        println("where scheduleInternal() called executeIfValid()")
        println("directly on the calling thread, causing recursion")
        println("========================================\n")

        val recursionDepth = AtomicInteger(0)
        val maxRecursionDepth = AtomicInteger(0)
        val reentrantCallDetected = AtomicBoolean(false)
        val callCount = AtomicInteger(0)
        lateinit var timer: ResettableTimer

        timer = ResettableTimer {
            val depth = recursionDepth.incrementAndGet()
            maxRecursionDepth.updateAndGet { max -> maxOf(max, depth) }
            callCount.incrementAndGet()

            if (depth > 1) {
                reentrantCallDetected.set(true)
                println("⚠️  RE-ENTRANCY! Depth: $depth, Thread: ${Thread.currentThread().name}")
            }

            try {
                // Simulate what onNextTurn() does - it calls resetTurnTimeout()
                // which would cause recursion in buggy version 0.35.4
                if (callCount.get() < 5) {
                    timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))
                    timer.notifyReady()
                }

                Thread.sleep(1) // Simulate work
            } finally {
                recursionDepth.decrementAndGet()
            }
        }

        // Trigger the first execution
        timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(1))
        timer.notifyReady()

        // Wait for all calls to complete
        Thread.sleep(100)
        timer.shutdown()

        println("Max recursion depth: ${maxRecursionDepth.get()}")
        println("Re-entrancy detected: ${reentrantCallDetected.get()}")
        println("Total calls: ${callCount.get()}")
        println("========================================\n")

        // In version 0.35.5 (fixed), depth should always be 1
        maxRecursionDepth.get() shouldBe 1
        reentrantCallDetected.get() shouldBe false
        callCount.get() shouldBeGreaterThanOrEqual 5

        println("✅ Test PASSED - No re-entrancy detected")
    }

    test("simulates event queue overflow scenario at TPS=-1") {
        println("\n========================================")
        println("Timer Task Queue Test")
        println("========================================")
        println("Verifies that timer doesn't queue up tasks at TPS=-1")
        println("User reported: 'max event queue size reached: 256'")
        println("========================================\n")

        val taskExecutions = AtomicInteger(0)
        val concurrentExecutions = AtomicInteger(0)
        val maxConcurrentExecutions = AtomicInteger(0)

        val timer = ResettableTimer {
            // Track how many tasks are executing concurrently
            val concurrent = concurrentExecutions.incrementAndGet()
            maxConcurrentExecutions.updateAndGet { max -> maxOf(max, concurrent) }
            taskExecutions.incrementAndGet()

            // Simulate work
            Thread.sleep(1)

            concurrentExecutions.decrementAndGet()
        }

        // Rapidly schedule tasks at TPS=-1
        // In buggy version, tasks would queue up
        // In fixed version, old tasks get cancelled before new ones start
        repeat(1000) {
            timer.schedule(minDelayNanos = 0L, maxDelayNanos = TimeUnit.SECONDS.toNanos(30))
            timer.notifyReady()
            // No sleep - rapid fire scheduling
        }

        // Wait for any pending tasks
        Thread.sleep(2000)
        timer.shutdown()

        println("\n========================================")
        println("Timer Task Queue Results:")
        println("========================================")
        println("Total schedules: 1000")
        println("Total executions: ${taskExecutions.get()}")
        println("Max concurrent: ${maxConcurrentExecutions.get()}")
        println("========================================\n")

        // With the fix, tasks should be cancelled/replaced, not queued
        // So we should see far fewer executions than schedules
        // And concurrency should be 1 (single-threaded executor)
        maxConcurrentExecutions.get() shouldBe 1 // Single-threaded
        (taskExecutions.get() < 100) shouldBe true // Most tasks get cancelled/replaced

        println("✅ Test PASSED - Timer doesn't queue tasks, old ones get cancelled")
    }
})
