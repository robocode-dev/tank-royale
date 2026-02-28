package dev.robocode.tankroyale.common.recording

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.zip.GZIPInputStream

class GameRecorderTest : FunSpec({

    context("GameRecorder") {

        test("creates a file with .battle.gz extension in the specified directory") {
            val tempDir = Files.createTempDirectory("recorder-test").toFile()
            try {
                val recorder = GameRecorder(tempDir.absolutePath)
                recorder.close()

                recorder.file.name shouldEndWith ".battle.gz"
                recorder.file.parentFile.absolutePath shouldBe tempDir.absolutePath
                recorder.file.exists() shouldBe true
            } finally {
                tempDir.deleteRecursively()
            }
        }

        test("records JSON lines as GZIP-compressed ND-JSON") {
            val tempDir = Files.createTempDirectory("recorder-test").toFile()
            try {
                val recorder = GameRecorder(tempDir.absolutePath)
                recorder.record("""{"type":"GameStartedEventForObserver","gameSetup":{}}""")
                recorder.record("""{"type":"TickEventForObserver","turnNumber":1}""")
                recorder.record("""{"type":"GameEndedEventForObserver","numberOfRounds":1}""")
                recorder.close()

                // Read back and verify
                val lines = GZIPInputStream(recorder.file.inputStream()).use { gzip ->
                    BufferedReader(InputStreamReader(gzip)).readLines()
                }

                lines.size shouldBe 3
                lines[0] shouldBe """{"type":"GameStartedEventForObserver","gameSetup":{}}"""
                lines[1] shouldBe """{"type":"TickEventForObserver","turnNumber":1}"""
                lines[2] shouldBe """{"type":"GameEndedEventForObserver","numberOfRounds":1}"""
            } finally {
                tempDir.deleteRecursively()
            }
        }

        test("RECORDABLE_EVENT_TYPES contains expected types") {
            GameRecorder.RECORDABLE_EVENT_TYPES shouldBe setOf(
                "GameAbortedEvent",
                "GameEndedEventForObserver",
                "GameStartedEventForObserver",
                "RoundEndedEventForObserver",
                "RoundStartedEventForObserver",
                "TickEventForObserver",
            )
        }

        test("START_RECORDING_TYPES contains GameStartedEventForObserver") {
            GameRecorder.START_RECORDING_TYPES shouldBe setOf("GameStartedEventForObserver")
        }

        test("END_RECORDING_TYPES contains end events") {
            GameRecorder.END_RECORDING_TYPES shouldBe setOf("GameAbortedEvent", "GameEndedEventForObserver")
        }
    }
})
