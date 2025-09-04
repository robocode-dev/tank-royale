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
        if (!ConfigSettings.enableAutoRecording) return

        // Ensure recorder process is running
        RecorderProcess.start()

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
    }
}
