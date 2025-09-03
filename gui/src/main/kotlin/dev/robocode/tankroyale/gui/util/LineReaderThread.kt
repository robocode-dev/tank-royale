package dev.robocode.tankroyale.gui.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

/**
 * Utility to read lines from an InputStream on a background thread and invoke a callback per line.
 * The thread can be started and stopped, and exposes a simple isRunning() state.
 */
class LineReaderThread(
    private val threadName: String,
    private val inputStream: InputStream,
    private val onLine: (String) -> Unit
) {

    private val threadRef = AtomicReference<Thread?>()

    fun start() {
        if (isRunning()) return
        val t = Thread {
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        val line = reader.readLine() ?: break
                        try {
                            onLine(line)
                        } catch (_: Exception) {
                            // Swallow callback exceptions to keep the reader alive
                        }
                    }
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (_: Exception) {
                    // Ignore; occurs when process closes stream or thread is interrupted
                }
            }
        }.apply {
            name = threadName
            isDaemon = true
            start()
        }
        threadRef.set(t)
    }

    fun stop() {
        threadRef.get()?.interrupt()
    }

    fun isRunning(): Boolean {
        val t = threadRef.get()
        return t != null && t.isAlive && !t.isInterrupted
    }
}