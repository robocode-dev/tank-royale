package dev.robocode.tankroyale.gui.recorder

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AutoRecorder wires battle lifecycle events to the RecorderProcess when enabled via settings.
 */
object AutoRecorder {

    private val recording = AtomicBoolean(false)

    val isRecording: Boolean
        get() = recording.get()

    init {
        ClientEvents.onGameStarted.subscribe(AutoRecorder) { _ -> onGameStarted() }
        ClientEvents.onGameEnded.subscribe(AutoRecorder) { _ -> onGameEndedOrAborted() }
        ClientEvents.onGameAborted.subscribe(AutoRecorder) { _ -> onGameEndedOrAborted() }
        ServerEvents.onStopped.subscribe(AutoRecorder) { onServerStopped() }
    }

    private fun onGameStarted() {
        if (!ConfigSettings.enableAutoRecording) {
            // Ensure recorder process is not running when auto-recording is disabled
            RecorderProcess.stop()
            return
        }

        // Ensure 'recordings' directory exists and start recorder with that directory
        val recordingsDir = java.nio.file.Paths.get("recordings")
        var dirArg: String? = null
        try {
            java.nio.file.Files.createDirectories(recordingsDir)
            dirArg = recordingsDir.toAbsolutePath().toString()
        } catch (_: Exception) {
            // If we cannot create the directory, we fall back to default behavior
        }

        // Ensure recorder process is running, prefer using the recordings directory when available
        if (dirArg != null) {
            RecorderProcess.start(dir = dirArg)
        } else {
            RecorderProcess.start()
        }

        // If a recording is already ongoing (e.g., on restart), stop before starting a new one
        if (recording.get()) {
            RecorderProcess.stopRecording()
        }
        RecorderProcess.startRecording()
        recording.set(true)
    }

    private fun onGameEndedOrAborted() {
        stopRecordingIfNeeded()
    }

    private fun onServerStopped() {
        stopRecordingIfNeeded()
        RecorderProcess.stop()
    }

    private fun stopRecordingIfNeeded() {
        if (recording.getAndSet(false)) {
            RecorderProcess.stopRecording()
        }
        // Always stop the recorder process between games to prevent unintended auto-recording on next game
        RecorderProcess.stop()
    }
}
