package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class RumbleTreeHashTest {

    private val files = mapOf(
        "Bot.json" to "{\"name\":\"Bot\"}\n".toByteArray(),
        "src/Main.java" to "class Bot {}\r\n".toByteArray(),
        "sub/z.txt" to "z".toByteArray(),
        "Zed.txt" to "last".toByteArray(),
    )

    // Produced by the bot repository's own tree_hash function for exactly these four files.
    private val expected = "sha256:670e2e29bb1550d596ef712899d20a130d575a208ffc06ff47ef2888a13e5dfe"

    @Test
    @Tag("RCL-022")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL022_UnitPositive_theHashEqualsTheBotRepositorysOwnAlgorithm() {
        assertEquals(expected, TreeHash.of(files))
        assertEquals(expected, TreeHash.of(files.entries.reversed().associate { it.key to it.value }))
    }

    @Test
    @Tag("RCL-022")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL022_UnitPositive_cacheFoldersAreSkippedAndFilesOnDiskGiveTheSameHash(@TempDir directory: Path) {
        val withCache = files + ("__pycache__/x.pyc" to byteArrayOf(1, 2, 3)) + ("sub/__pycache__/y.pyc" to byteArrayOf(4))
        assertEquals(expected, TreeHash.of(withCache))

        withCache.forEach { (path, bytes) ->
            val file = directory.resolve(path)
            Files.createDirectories(file.parent)
            Files.write(file, bytes)
        }
        assertEquals(expected, TreeHash.ofDirectory(directory))
    }

    @Test
    @Tag("RCL-022")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL022_UnitNegative_anyChangeToBytesNameOrPathChangesTheHash() {
        val changedBytes = files + ("sub/z.txt" to "y".toByteArray())
        val lineEndings = files + ("src/Main.java" to "class Bot {}\n".toByteArray())
        val renamed = files - "Zed.txt" + ("Zed2.txt" to "last".toByteArray())
        val moved = files - "sub/z.txt" + ("other/z.txt" to "z".toByteArray())
        val removed = files - "Zed.txt"

        listOf(changedBytes, lineEndings, renamed, moved, removed).forEach {
            assertNotEquals(expected, TreeHash.of(it))
        }
    }
}
