package dev.robocode.tankroyale.gui.audio

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

class Sound(private val audioData: ByteArray) {

    companion object {
        private const val MIN_VOLUME = 0
        private const val MAX_VOLUME = 100

        fun fromFile(filename: String): Sound {
            val bytes = try {
                File(filename).readBytes()
            } catch (_: Exception) {
                // Fall back to empty on any read error to preserve existing behavior
                ByteArray(0)
            }
            return Sound(bytes)
        }
    }

    fun play() {
        if (audioData.isEmpty()) return

        val inputStream = BufferedInputStream(ByteArrayInputStream(audioData))
        val audioStream = AudioSystem.getAudioInputStream(inputStream)
        AudioSystem.getClip().apply {
            addLineListener {
                if (it.type == LineEvent.Type.STOP) {
                    close()
                    audioStream.close()
                }
            }
            open(audioStream)
            try {
                val volume = ConfigSettings.soundVolume.coerceIn(MIN_VOLUME, MAX_VOLUME) / 100f
                applyVolume(volume)
            } catch (_: Exception) {
                // Ignore if volume control is not supported or fails
            }
            start()
        }
    }

    private fun javax.sound.sampled.Clip.applyVolume(vol: Float) {
        when {
            isControlSupported(FloatControl.Type.MASTER_GAIN) -> {
                val ctrl = getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                ctrl.setNormalizedValue(vol)
            }
            isControlSupported(FloatControl.Type.VOLUME) -> {
                val ctrl = getControl(FloatControl.Type.VOLUME) as FloatControl
                ctrl.setNormalizedValue(vol)
            }
        }
    }

    private fun FloatControl.setNormalizedValue(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        value = minimum + (maximum - minimum) * clamped
    }
}