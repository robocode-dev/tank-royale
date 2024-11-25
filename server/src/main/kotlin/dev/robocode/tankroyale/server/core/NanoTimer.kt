package dev.robocode.tankroyale.server.core

import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/** NanoTimer is a high-resolution timer with a high precision. */
class NanoTimer(
    /** Time in nanoseconds between successive job executions */
    private val periodInNanos: Long,

    /** Job to execute when timer is triggered */
    private val job: Runnable
) {
    private var thread: Thread? = null
    private val isRunning = AtomicBoolean()
    private val isPaused = AtomicBoolean()
    private var lastTime = System.nanoTime()

    /** Starts the timer. */
    fun start() {
        thread = Thread { run() }
        isRunning.set(true)
        thread?.start()
    }

    /** Stops the timer. */
    fun stop() {
        isRunning.set(false)
        thread?.interrupt()
    }

    /** Pauses the timer. */
    fun pause() {
        isPaused.set(true)
    }

    /** Resumes the timer after having been paused. */
    fun resume() {
        isPaused.set(false)
    }

    /** Resets timer. */
    fun reset() {
        lastTime = System.nanoTime()
    }

    private fun run() {
        while (isRunning.get() && !thread!!.isInterrupted) {
            val now = System.nanoTime()
            val diff = periodInNanos - (now - lastTime)
            if (diff <= 0) {
                lastTime = now
                if (!isPaused.get()) {
                    job.run()
                }
            } else {
                Thread.sleep(Duration.ofNanos(diff))
            }
        }
        thread = null
    }
}
