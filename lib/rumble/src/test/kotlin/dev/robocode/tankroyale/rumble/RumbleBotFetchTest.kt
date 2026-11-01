package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.streams.toList

class RumbleBotFetchTest {

    private val commit = "05c4ecfd6c67ec33e10afa85aa0bc9b81df20416"

    private val walls = mapOf(
        "Walls.json" to "{\"name\":\"Walls\"}\n".toByteArray(),
        "Walls.cmd" to "@echo off\r\njava Walls\r\n".toByteArray(),
        "src/Walls.java" to "class Walls {}\r\n".toByteArray(),
    )
    private val orbit = mapOf(
        "Orbit.json" to "{\"name\":\"Orbit\"}\n".toByteArray(),
        "Orbit.py" to "print('orbit')\n".toByteArray(),
    )

    private fun bot(name: String, platform: String, files: Map<String, ByteArray>, hash: String = TreeHash.of(files)) =
        CatalogBot(
            name = name, version = "1.0", platform = platform, path = "bots/$platform/$name", sourceHash = hash,
            owner = "someone", authors = listOf("A. Author"), addedAt = "2026-10-01", status = "active",
        )

    private fun zip(entries: List<Pair<String, ByteArray>>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content)
                zip.closeEntry()
            }
        }
        return bytes.toByteArray()
    }

    private fun repositoryEntries(vararg trees: Pair<String, Map<String, ByteArray>>): List<Pair<String, ByteArray>> =
        listOf("rumble-bots-$commit/README.md" to "readme".toByteArray()) +
            trees.flatMap { (folder, files) -> files.map { "rumble-bots-$commit/$folder/${it.key}" to it.value } }

    private fun source(entries: List<Pair<String, ByteArray>>) = object : CatalogSource {
        val archive = zip(entries)
        override fun catalogText(commit: String) = error("not used")
        override fun archive(commit: String): InputStream = ByteArrayInputStream(archive)
    }

    private fun leftovers(target: Path): List<String> =
        Files.list(target).use { paths -> paths.map { it.fileName.toString() }.sorted().toList() }

    @Test
    @Tag("RCL-023")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCL023_IntegrationPositive_matchingBotsArePlacedByteForByteAndNothingElseIsLeft(@TempDir target: Path) {
        val archive = source(repositoryEntries("bots/java/Walls" to walls, "bots/python/Orbit" to orbit))

        val results = BotFetcher.fetch(
            archive, commit, listOf(bot("Walls", "java", walls), bot("Orbit", "python", orbit)), target
        )

        assertEquals(2, results.size)
        results.forEach { assertInstanceOf(FetchResult.Placed::class.java, it) }
        walls.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(target.resolve("Walls").resolve(path))) }
        orbit.forEach { (path, bytes) -> assertArrayEquals(bytes, Files.readAllBytes(target.resolve("Orbit").resolve(path))) }
        assertEquals(listOf("Orbit", "Walls"), leftovers(target))
    }

    @Test
    @Tag("RCL-023")
    @Tag("Integration")
    @Tag("Negative")
    fun testRCL023_IntegrationNegative_aBotWithAnotherHashOrMissingFilesIsReportedAndNothingOfItIsLeft(@TempDir target: Path) {
        val wrongHash = bot("Walls", "java", walls, hash = "sha256:" + "0".repeat(64))
        // The archive misses a file that the catalog hash covers, as an archive does for export-ignored files.
        val missingFile = bot("Orbit", "python", orbit)
        val partialOrbit = orbit - "Orbit.py"
        val absent = bot("Ghost", "java", walls)
        val archive = source(
            repositoryEntries("bots/java/Walls" to walls, "bots/python/Orbit" to partialOrbit)
        )

        val results = BotFetcher.fetch(archive, commit, listOf(wrongHash, missingFile, absent), target)

        val mismatch = assertInstanceOf(FetchResult.HashMismatch::class.java, results[0])
        assertEquals(TreeHash.of(walls), mismatch.actual)
        assertInstanceOf(FetchResult.HashMismatch::class.java, results[1])
        assertInstanceOf(FetchResult.NotInArchive::class.java, results[2])
        assertEquals(emptyList<String>(), leftovers(target))
    }

    @Test
    @Tag("RCL-023")
    @Tag("Integration")
    @Tag("Negative")
    fun testRCL023_IntegrationNegative_oneFailingBotDoesNotStopTheOthersAndAnExistingFolderIsNeverReplaced(@TempDir target: Path) {
        val own = target.resolve("Orbit")
        Files.createDirectories(own)
        Files.writeString(own.resolve("mine.txt"), "my own bot")
        val archive = source(repositoryEntries("bots/java/Walls" to walls, "bots/python/Orbit" to orbit))

        val results = BotFetcher.fetch(
            archive, commit,
            listOf(bot("Orbit", "python", orbit), bot("Walls", "java", walls, hash = "sha256:bad"), bot("Walls2", "java", walls)),
            target,
        )

        assertEquals(own, assertInstanceOf(FetchResult.AlreadyExists::class.java, results[0]).directory)
        assertInstanceOf(FetchResult.HashMismatch::class.java, results[1])
        assertInstanceOf(FetchResult.NotInArchive::class.java, results[2])
        assertEquals("my own bot", Files.readString(own.resolve("mine.txt")))
        assertFalse(Files.exists(own.resolve("Orbit.json")))
        assertEquals(listOf("Orbit"), leftovers(target))
    }

    @Test
    @Tag("RCL-024")
    @Tag("Integration")
    @Tag("Negative")
    fun testRCL024_IntegrationNegative_anArchiveOrCatalogPathThatEscapesIsRefusedAndNothingIsWrittenOutside(@TempDir base: Path) {
        val target = Files.createDirectories(base.resolve("bots-folder"))
        val sneaky = source(
            repositoryEntries("bots/java/Walls" to walls) + ("rumble-bots-$commit/../../evil.txt" to "x".toByteArray())
        )
        assertThrows(UnsafeFetch::class.java) { BotFetcher.fetch(sneaky, commit, listOf(bot("Walls", "java", walls)), target) }

        val backslash = source(repositoryEntries("bots/java/Walls" to walls) + ("rumble-bots-$commit/..\\evil.txt" to "x".toByteArray()))
        assertThrows(UnsafeFetch::class.java) { BotFetcher.fetch(backslash, commit, listOf(bot("Walls", "java", walls)), target) }

        val honest = source(repositoryEntries("bots/java/Walls" to walls))
        listOf("../escape", "/etc/passwd", "bots/../../x", "bots//java").forEach { path ->
            val entry = bot("Walls", "java", walls).copy(path = path)
            assertThrows(UnsafeFetch::class.java, { BotFetcher.fetch(honest, commit, listOf(entry), target) }, path)
        }

        assertEquals(listOf("bots-folder"), leftovers(base))
        assertEquals(emptyList<String>(), leftovers(target))
    }

    @Test
    @Tag("RCL-024")
    @Tag("Integration")
    @Tag("Positive")
    fun testRCL024_IntegrationPositive_anOrdinaryArchiveIsFetchedAndUnrelatedFilesAreNotExtracted(@TempDir target: Path) {
        val archive = source(repositoryEntries("bots/java/Walls" to walls, "bots/python/Orbit" to orbit))

        val results = BotFetcher.fetch(archive, commit, listOf(bot("Walls", "java", walls)), target)

        assertInstanceOf(FetchResult.Placed::class.java, results.single())
        assertEquals(listOf("Walls"), leftovers(target))
        assertTrue(Files.exists(target.resolve("Walls").resolve("src").resolve("Walls.java")))
    }

    @Test
    @Tag("Sanity")
    fun testSanity_githubAddressesAreBuiltFromTheEnvironmentAndOnlyAcceptHexadecimalCommits() {
        val testSource = GitHubCatalogSource(RumbleEnvironments.TEST)

        assertEquals(
            "https://raw.githubusercontent.com/robocode-dev/rumble-bots-test/$commit/bots/index.json",
            testSource.catalogUrl(commit).toString(),
        )
        assertEquals(
            "https://codeload.github.com/robocode-dev/rumble-bots-test/zip/$commit",
            testSource.archiveUrl(commit).toString(),
        )
        listOf("", "main", "../x", "abc/def", "$commit?x=1", "ABCDEF1").forEach {
            assertThrows(CatalogUnreadable::class.java, { testSource.archiveUrl(it) }, it)
        }
    }

    @Test
    @Tag("Sanity")
    fun testSanity_liveCatalogMatchesItsSourcesWhenAskedTo() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to read the public Rumble")
        val live = GitHubCatalogSource(RumbleEnvironments.PUBLIC)
        val catalogCommit = Catalog.parse(
            java.net.URI.create("https://raw.githubusercontent.com/robocode-dev/rumble-bots/main/bots/index.json")
                .toURL().readText()
        ).commit
        val catalog = Catalog.parse(live.catalogText(catalogCommit))
        val target = Files.createTempDirectory("rumble-live")

        try {
            val results = BotFetcher.fetch(live, catalog.commit, catalog.activeBots, target)
            assertEquals(catalog.activeBots.size, results.filterIsInstance<FetchResult.Placed>().size, results.toString())
        } finally {
            target.toFile().deleteRecursively()
        }
    }
}
