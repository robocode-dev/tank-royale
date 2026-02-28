package dev.robocode.tankroyale.common.recording

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPOutputStream

/**
 * Writes battle events as GZIP-compressed ND-JSON (newline-delimited JSON) files.
 *
 * Each call to [record] writes one JSON line. The output file is named
 * `game-{timestamp}.battle.gz` inside the specified directory.
 *
 * This class is the shared core used by both the standalone Recorder module and the
 * Battle Runner API for battle recording.
 *
 * @param dir directory to write the recording file to, or `null` for the current directory
 */
class GameRecorder(dir: String?) : AutoCloseable {

    /** The recording output file. */
    val file: File

    private val output: PrintWriter

    init {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
        file = File(dir, "game-$now.battle.gz")
        output = PrintWriter(GZIPOutputStream(FileOutputStream(file)))
    }

    /**
     * Writes a single JSON line to the recording file.
     *
     * @param jsonLine a compact JSON string (one event)
     */
    fun record(jsonLine: String) {
        output.println(jsonLine)
    }

    override fun close() {
        output.close()
    }

    companion object {
        /** Server message types that should be recorded (matches the Recorder module's event set). */
        val RECORDABLE_EVENT_TYPES: Set<String> = setOf(
            "GameAbortedEvent",
            "GameEndedEventForObserver",
            "GameStartedEventForObserver",
            "RoundEndedEventForObserver",
            "RoundStartedEventForObserver",
            "TickEventForObserver",
        )

        /** Event types that signal the start of a new recording. */
        val START_RECORDING_TYPES: Set<String> = setOf("GameStartedEventForObserver")

        /** Event types that signal the end of a recording. */
        val END_RECORDING_TYPES: Set<String> = setOf("GameAbortedEvent", "GameEndedEventForObserver")
    }
}
