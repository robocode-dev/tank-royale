package dev.robocode.tankroyale.rumble.worker

import dev.robocode.tankroyale.botproject.TreeHash
import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.rumble.CatalogBot
import dev.robocode.tankroyale.rumble.CatalogSource
import dev.robocode.tankroyale.rumble.JournalClient
import dev.robocode.tankroyale.rumble.JournalEngine
import dev.robocode.tankroyale.rumble.RankedJournal
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.SelectionMode
import dev.robocode.tankroyale.rumble.SessionControl
import dev.robocode.tankroyale.rumble.SessionEnd
import dev.robocode.tankroyale.rumble.SessionEvent
import dev.robocode.tankroyale.rumble.SessionRequest
import dev.robocode.tankroyale.rumble.SessionRunner
import dev.robocode.tankroyale.rumble.SessionSetup
import dev.robocode.tankroyale.rumble.WorkerCommand
import dev.robocode.tankroyale.rumble.WorkerHost
import dev.robocode.tankroyale.rumble.WorkerLimits
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Runs a contribution session with the real battle worker and two sample bots that are placed from a catalog source, and
 * keeps the result in a journal.
 *
 * Run via `./gradlew :rumble:integrationTest` — excluded from the default `test` task.
 */
@Tag("integration")
class WorkerSessionIntegrationTest {

    private val commit = "b".repeat(40)

    private fun sampleBot(name: String): Path {
        val root = System.getProperty("sampleBots.java.dir") ?: error("System property 'sampleBots.java.dir' not set — run via :rumble:integrationTest")
        return Path.of(root).resolve(name).also { check(Files.exists(it)) { "Sample bot not found: $it" } }
    }

    private fun workerCommand() = WorkerCommand { folders ->
        listOf(
            File(System.getProperty("java.home"), "bin/java").absolutePath,
            "-Xmx512m",
            "-Duser.home=${folders.home}",
            "-Djava.io.tmpdir=${folders.temp}",
            "-cp", System.getProperty("rumble.worker.classpath") ?: error("System property 'rumble.worker.classpath' not set"),
            "dev.robocode.tankroyale.rumble.worker.MainKt",
            "worker",
        )
    }

    /**
     * The files of a sample bot as a bot repository would hold them: the bot's own files and a launcher that names the Bot API
     * by its full path, because a sample bot expects the Bot API in a folder beside it, which a placed bot does not have.
     */
    private fun tree(name: String): Map<String, ByteArray> {
        val root = sampleBot(name)
        val lib = sampleBot("lib").toString()
        val files = HashMap<String, ByteArray>()
        root.toFile().walkTopDown().filter { it.isFile }.forEach { files[root.relativize(it.toPath()).toString().replace('\\', '/')] = it.readBytes() }
        val launcher = "java -cp \"$lib/*\" $name.java\n"
        files["$name.cmd"] = ("@echo off\r\n" + launcher).toByteArray()
        files["$name.sh"] = ("#!/bin/sh\n" + launcher).toByteArray()
        return files
    }

    /** The sample bots of the given folders, served as the archive of a bot repository. */
    private fun sourceOf(vararg folders: String) = object : CatalogSource {
        override fun catalogText(commit: String) = error("not used")
        override fun archive(commit: String): InputStream {
            val bytes = ByteArrayOutputStream()
            ZipOutputStream(bytes).use { zip ->
                for (name in folders) {
                    for ((path, content) in tree(name)) {
                        zip.putNextEntry(ZipEntry("rumble-bots-$commit/bots/java/$name/$path"))
                        zip.write(content)
                        zip.closeEntry()
                    }
                }
            }
            return ByteArrayInputStream(bytes.toByteArray())
        }
    }

    // RCL-120

    @Test
    @Tag("RCL-120")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCL120_IntegrationPositive_aRealBattleThroughTheRealWorkerIsKeptInTheJournalAndReadBack(@TempDir dir: Path) {
        // The catalog names a bot as the bot itself does, so the sample "SpinBot" folder holds the bot "Spin Bot".
        val walls = CatalogBot("Walls", "1.0", "JVM", "bots/java/Walls", TreeHash.of(tree("Walls")), "owner", listOf("Author"), "2026-01-01", CatalogBot.ACTIVE)
        val spin = CatalogBot("Spin Bot", "1.0", "JVM", "bots/java/SpinBot", TreeHash.of(tree("SpinBot")), "owner", listOf("Author"), "2026-01-01", CatalogBot.ACTIVE)
        val journalFile = dir.resolve("journal.jsonl")
        val journal = RankedJournal.open(RumbleEnvironments.TEST, journalFile)
        val engine = JournalEngine(CURRENT_BEHAVIOR_VERSION, "test", "test")
        val setup = SessionSetup(
            commit = commit, source = sourceOf("Walls", "SpinBot"), botCache = dir.resolve("cache"), host = WorkerHost(WorkerLimits(totalTime = Duration.ofMinutes(3))),
            command = workerCommand(), journal = journal, engine = engine, client = JournalClient("integration", "test"), roundsPerFormat = mapOf("1v1" to 1),
        )
        val events = mutableListOf<SessionEvent>()
        val request = SessionRequest(listOf(walls, spin), emptySet(), SelectionMode.ALL, listOf("1v1"), mapOf("1v1" to 2), battleLimit = 1)

        val end = SessionRunner(setup, SessionControl()) { events += it }.run { request }

        assertEquals(SessionEnd.Finished, end, events.toString())
        val record = RankedJournal.open(RumbleEnvironments.TEST, journalFile).entries().single().record
        assertEquals(mapOf("Walls" to walls.sourceHash, "Spin Bot" to spin.sourceHash), record.participants.associate { it.name to it.sourceHash })
        assertEquals(listOf(1, 2), record.participants.map { it.rank }.sorted())
        assertEquals(CURRENT_BEHAVIOR_VERSION, record.engine.behaviorVersion)
        assertEquals(commit, record.botsRepoCommit)
        assertTrue(events.any { it is SessionEvent.BattleKept })
    }
}
