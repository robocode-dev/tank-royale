package dev.robocode.tankroyale.recorder.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPOutputStream

class GameRecorder (
    dir: String?,
): AutoCloseable {
    companion object {
        private val ndJson = Json { prettyPrint = false }
    }

    val file: File
    private val output: PrintWriter

    init {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
        file = File(dir, "game-$now.battle.gz")
        output = PrintWriter(GZIPOutputStream(FileOutputStream(file)))
    }

    fun record(event: JsonElement) {
        output.println(ndJson.encodeToString(JsonElement.serializer(), event))
    }

    override fun close() {
        output.close()
    }
}
