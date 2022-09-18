package dev.robocode.tankroyale.gui.audio

import java.io.*
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

class Sound(private val byteArray: ByteArray) {

    companion object {
        fun fromFile(filename: String): Sound {
            var byteArray: ByteArray
            try {
                val file = File(filename)
                val fileInputStream = FileInputStream(file)
                byteArray = ByteArray(file.length().toInt())
                fileInputStream.read(byteArray)
                fileInputStream.close()
            } catch (ex: Exception) {
                byteArray = ByteArray(0)
            }
            return Sound(byteArray)
        }
    }

    fun play() {
        if (byteArray.isEmpty()) return

        val inputStream = BufferedInputStream(ByteArrayInputStream(byteArray))
        val audioStream = AudioSystem.getAudioInputStream(inputStream)
        AudioSystem.getClip().apply {
            addLineListener {
                if (it.type == LineEvent.Type.STOP) {
                    close()
                    audioStream.close()
                }
            }
            open(audioStream)
            start()
        }
    }
}