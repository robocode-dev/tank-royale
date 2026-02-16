package dev.robocode.tankroyale.gui.recorder

import dev.robocode.tankroyale.common.event.On
import dev.robocode.tankroyale.common.RECORDINGS_DIR
import dev.robocode.tankroyale.common.util.UserDataDirectory
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
        ClientEvents.onGameStarted+= On(AutoRecorder) { _ -> onGameStarted() }
        ClientEvents.onGameEnded+= On(AutoRecorder) { _ -> onGameEndedOrAborted() }
        ClientEvents.onGameAborted+= On(AutoRecorder) { _ -> onGameEndedOrAborted() }
        ServerEvents.onStopped+= On(AutoRecorder) { onServerStopped() }
    }

    private fun onGameStarted() {
        if (!ConfigSettings.enableAutoRecording) {
            // Ensure a recorder process is not running when auto-recording is disabled
            RecorderProcess.stop()
            return
        }

        // Get the recordings directory in the user data directory
        val recordingsDir = UserDataDirectory.getSubdir(RECORDINGS_DIR)
        val dirArg = recordingsDir.absolutePath

        // Ensure a recorder process is running with the recordings directory
        RecorderProcess.start(dir = dirArg)

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
