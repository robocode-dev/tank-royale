package dev.robocode.tankroyale.gui.audio

import java.io.*
import java.lang.Thread.sleep
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

class Sound(private val byteArray: ByteArray) {

    companion object {
        fun fromFile(filename: String): Sound {
            val file = File(filename)
            val fileInputStream = FileInputStream(file)
            val byteArray = ByteArray(file.length().toInt())
            fileInputStream.read(byteArray)
            fileInputStream.close()
            return Sound(byteArray)
        }
    }

    fun play() {
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

fun main() {
    val sound1 = Sound.fromFile("sounds/gunshot.wav")
    val sound2 = Sound.fromFile("sounds/wall_collision.wav")
    val sound3 = Sound.fromFile("sounds/death.wav")

    sound3.play()

    for (i in 1..2) {
        sleep(200)
        sound1.play()
        sleep(200)
        sound2.play()
    }

    sleep(2000)
}