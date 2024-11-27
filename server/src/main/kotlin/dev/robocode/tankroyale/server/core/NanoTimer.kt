package dev.robocode.tankroyale.server.core

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** NanoTimer is a high-resolution timer with a high precision. */
class NanoTimer(
    /** Minimum time in nanoseconds before job can be executed */
    private val minPeriodInNanos: Long,

    /** Maximum time in nanoseconds before job is definitely executed */
    private val maxPeriodInNanos: Long,

    /** Job to execute when timer is triggered */
    private val job: Runnable
) : Runnable {
    private var thread: Thread? = null
    private val jobExecuted = AtomicBoolean(false)
    private var ready = AtomicBoolean(false)
    private var pauseStartTime = AtomicLong(0L)
    private var totalPauseDuration = AtomicLong(0L)

    /** Starts the timer. */
    fun start() {
        thread = Thread(this)
        thread?.start()
    }

    /** Stops the timer. */
    fun stop(): Boolean {
        // This will prevent any other threads from executing the job, even if we haven't
        val success = !jobExecuted.getAndSet(true)
        thread?.interrupt()
        return success
    }

    /** Pauses the timer. */
    fun pause() {
        pauseStartTime.compareAndSet(0L, System.nanoTime())
    }

    /** Resumes the timer after having been paused. */
    fun resume() {
        val pauseStart = pauseStartTime.getAndSet(0L)
        if (pauseStart == 0L) return

        val diff = System.nanoTime() - pauseStart
        totalPauseDuration.addAndGet(diff)

        thread?.interrupt()
    }

    /** Notifies that the task is ready to be executed. */
    fun notifyReady() {
        ready.set(true)
        thread?.interrupt()
    }

    override fun run() {
        val lastTime = System.nanoTime()
        if (minPeriodInNanos > 0) {
            while (true) {
                val now = System.nanoTime()
                val diff = lastTime + minPeriodInNanos + totalPauseDuration.get() - now
                val isPaused = pauseStartTime.get() != 0L
                if (diff <= 0 && !isPaused) break
                try {
                    if (isPaused) {
                        TimeUnit.SECONDS.sleep(1)
                    } else {
                        TimeUnit.NANOSECONDS.sleep(diff)
                    }
                } catch (e: InterruptedException) {
                    if (jobExecuted.get()) return
                }
            }
        }

        while (true) {
            if (jobExecuted.get()) break

            val now = System.nanoTime()
            val diff = lastTime + maxPeriodInNanos + totalPauseDuration.get() - now
            val isPaused = pauseStartTime.get() != 0L
            if (ready.get() || diff <= 0 && !isPaused) {
                executeJob()
                break
            } else {
                try {
                    if (isPaused) {
                        TimeUnit.SECONDS.sleep(1)
                    } else {
                        TimeUnit.NANOSECONDS.sleep(diff)
                    }
                } catch (e: InterruptedException) {
                    continue
                }
            }
        }

        thread = null
    }

    private fun executeJob() {
        if (jobExecuted.getAndSet(true)) return
        job.run()
    }
}
