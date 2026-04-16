package dev.robocode.tankroyale.recorder.cli

import dev.robocode.tankroyale.recorder.core.RecordingObserver
import dev.robocode.tankroyale.recorder.util.VersionFileProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.system.exitProcess

/**
 * Runtime controller for the Robocode Tank Royale recorder.
 *
 * Thread model: [run] executes on the calling (main) thread and blocks until the WebSocket
 * connection closes. A separate stdin monitor thread reads commands from standard input.
 * The [recordingObserver] field is declared `@Volatile` to guarantee visibility across both
 * threads without requiring explicit locking.
 */
class RecorderRuntime(
    private val url: String = DEFAULT_URL,
    private val secret: String? = null,
    private val dir: String? = null,
) : Runnable {

    companion object {
        private const val EXIT_COMMAND = "quit"
        private const val STOP_COMMAND = "stop"
        private const val ABORT_COMMAND = "abort"
        private const val START_COMMAND = "start"

        private const val NOT_INITIALIZED_WARNING = "Recorder is not initialized yet. Please wait and try again."

        const val DEFAULT_URL: String = "ws://localhost:7654"
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    @Volatile
    private var recordingObserver: RecordingObserver? = null

    private val stdinMonitorExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "stdin-monitor").apply { isDaemon = true }
    }

    /** Starts the recorder, blocks until the server connection closes, then shuts down the stdin monitor. */
    override fun run() {
        println(VersionFileProvider.getVersion())
        stdinMonitorExecutor.submit { monitorStandardInputForExit() }
        val canonicalDir = File(dir, ".").canonicalPath
        log.info("Recordings will be stored in $canonicalDir")
        try {
            startRecorder()
        } finally {
            stdinMonitorExecutor.shutdownNow()
        }
    }

    private fun monitorStandardInputForExit() {
        Scanner(System.`in`).use { scanner ->
            while (scanner.hasNextLine()) {
                val input = scanner.nextLine().trim()
                handleInput(input)
            }
        }
    }

    private fun handleInput(input: String) {
        when {
            input.equals(EXIT_COMMAND, ignoreCase = true) -> handleExit()
            input.equals(START_COMMAND, ignoreCase = true) -> handleStart()
            input.equals(STOP_COMMAND, ignoreCase = true) -> handleStop()
            input.equals(ABORT_COMMAND, ignoreCase = true) -> handleAbort()
        }
    }

    private fun handleExit() {
        recordingObserver?.stop()
        exitProcess(0)
    }

    private fun startRecorder() {
        val observer = RecordingObserver(url, secret, dir)
        recordingObserver = observer
        observer.start()
        observer.awaitClose()
    }

    private fun handleStart() {
        val observer = recordingObserver ?: run {
            log.warn(NOT_INITIALIZED_WARNING)
            return
        }
        if (!observer.isRecording()) {
            observer.startRecording()
            log.info("Recording started.")
        } else {
            log.info("Recording is already started.")
        }
    }

    private fun handleStop() {
        val observer = recordingObserver ?: run {
            log.warn(NOT_INITIALIZED_WARNING)
            return
        }
        if (observer.isRecording()) {
            observer.stopRecordingKeepFile()
        } else {
            log.info("No active recording to stop.")
        }
    }

    private fun handleAbort() {
        val observer = recordingObserver ?: run {
            log.warn(NOT_INITIALIZED_WARNING)
            return
        }
        if (observer.isRecording()) {
            observer.stopAndDeleteRecording()
        } else {
            log.info("No active recording to abort.")
        }
    }
}
