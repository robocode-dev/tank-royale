package dev.robocode.tankroyale.rumble

/**
 * Runs [call] for a screen's work away from its thread, and gives its result or what it threw, so the screen can always
 * show an ending and never waits for an answer that will not come, also after an error that is not an exception, such
 * as running out of memory. What was thrown is written to the error output, so a failure on the screen can still be
 * traced, and an interruption keeps the thread's interrupt flag.
 */
internal inline fun <T> attempt(call: () -> T): Result<T> =
    try {
        Result.success(call())
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        Result.failure(e)
    } catch (e: Throwable) {
        e.printStackTrace()
        Result.failure(e)
    }
