package dev.robocode.tankroyale.server.core

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A resettable timer that reuses a single scheduled executor thread.
 *
 * The timer guarantees that the job runs no earlier than the configured minimum delay and
 * no later than the maximum delay after scheduling. A call to [notifyReady] requests execution
 * as soon as the minimum delay has elapsed. Paused time is excluded from timing calculations.
 */
class ResettableTimer(
    /** Job to execute when the timer triggers. */
    private val job: Runnable
) {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "TurnTimeoutTimer").apply { isDaemon = true }
    }

    private val lock = Any()
    private var scheduledFuture: ScheduledFuture<*>? = null
    private var active = false
    private var ready = false
    private var jobExecuted = false
    private var generation = 0L
    private var minDelayNanos = 0L
    private var maxDelayNanos = 0L
    private var startTimeNanos = 0L
    private var pauseStartTimeNanos = 0L
    private var totalPauseNanos = 0L

    /**
     * Schedules the timer with minimum and maximum delays in nanoseconds.
     *
     * The job will not execute before [minDelayNanos] and will execute no later than
     * [maxDelayNanos], unless canceled.
     */
    fun schedule(minDelayNanos: Long, maxDelayNanos: Long) {
        require(minDelayNanos >= 0L) { "minDelayNanos must be non-negative, got: $minDelayNanos" }
        require(maxDelayNanos >= 0L) { "maxDelayNanos must be non-negative, got: $maxDelayNanos" }
        require(maxDelayNanos >= minDelayNanos) {
            "maxDelayNanos ($maxDelayNanos) must be >= minDelayNanos ($minDelayNanos). " +
            "This may indicate turnTimeout is configured smaller than the minimum turn period (1/TPS)."
        }

        synchronized(lock) {
            active = true
            ready = false
            jobExecuted = false
            generation += 1

            this.minDelayNanos = minDelayNanos
            this.maxDelayNanos = maxDelayNanos
            startTimeNanos = System.nanoTime()
            pauseStartTimeNanos = 0L
            totalPauseNanos = 0L

            scheduleInternal(maxDelayNanos, generation)
        }
    }

    /**
     * Notifies the timer that the job is ready to execute once the minimum delay has elapsed.
     */
    fun notifyReady() {
        synchronized(lock) {
            if (!active) return
            ready = true
            if (isPaused()) return

            val remainingMin = minDelayNanos - elapsedNanos(System.nanoTime())
            scheduleInternal(remainingMin.coerceAtLeast(0L), generation)
        }
    }

    /** Cancels the current scheduled execution. */
    fun cancel() {
        synchronized(lock) {
            if (!active) return
            active = false
            ready = false
            generation += 1
            cancelScheduled()
        }
    }

    /** Pauses the timer, preventing execution until [resume] is called. */
    fun pause() {
        synchronized(lock) {
            if (!active || isPaused()) return
            pauseStartTimeNanos = System.nanoTime()
            cancelScheduled()
        }
    }

    /** Resumes the timer after a pause and recalculates remaining delay. */
    fun resume() {
        synchronized(lock) {
            if (!active || !isPaused()) return

            val now = System.nanoTime()
            totalPauseNanos += now - pauseStartTimeNanos
            pauseStartTimeNanos = 0L

            val elapsed = elapsedNanos(now)
            val remainingMax = (maxDelayNanos - elapsed).coerceAtLeast(0L)
            val remainingMin = (minDelayNanos - elapsed).coerceAtLeast(0L)

            val delay = if (ready) remainingMin else remainingMax
            scheduleInternal(delay, generation)
        }
    }

    /**
     * Shuts down the timer executor gracefully, forcing termination if needed.
     */
    fun shutdown() {
        synchronized(lock) {
            active = false
            ready = false
            generation += 1
            cancelScheduled()
        }

        executor.shutdown()
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    private fun scheduleInternal(delayNanos: Long, generationId: Long) {
        cancelScheduled()
        if (delayNanos <= 0L) {
            executor.execute { executeIfValid(generationId) }
        } else {
            scheduledFuture = executor.schedule({ executeIfValid(generationId) }, delayNanos, TimeUnit.NANOSECONDS)
        }
    }

    private fun cancelScheduled() {
        scheduledFuture?.cancel(false)
        scheduledFuture = null
    }

    private fun executeIfValid(generationId: Long) {
        synchronized(lock) {
            if (!active || jobExecuted || generationId != generation) return
            jobExecuted = true
            active = false
        }

        job.run()
    }

    private fun elapsedNanos(now: Long): Long = now - startTimeNanos - totalPauseNanos

    private fun isPaused() = pauseStartTimeNanos != 0L
}
