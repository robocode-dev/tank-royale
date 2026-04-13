package dev.robocode.tankroyale.runner

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectory

/**
 * Unit tests for [BotEntry] — construction, validation, and static factories.
 */
class BotEntryTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `of(Path) accepts existing directory`() {
        val dir = tempDir.resolve("MyBot").createDirectory()
        val entry = BotEntry.of(dir)
        assertThat(entry.path).isEqualTo(dir)
    }

    @Test
    fun `of(String) accepts existing directory`() {
        val dir = tempDir.resolve("MyBot").createDirectory()
        val entry = BotEntry.of(dir.toString())
        assertThat(entry.path).isEqualTo(dir)
    }

    @Test
    fun `constructor rejects nonexistent path`() {
        assertThatThrownBy { BotEntry(tempDir.resolve("NoSuchBot")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Bot path must be a directory")
    }

    @Test
    fun `constructor rejects file path`() {
        val file = tempDir.resolve("notADir.txt")
        file.toFile().createNewFile()
        assertThatThrownBy { BotEntry(file) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Bot path must be a directory")
    }

    @Test
    fun `data class equality works`() {
        val dir = tempDir.resolve("SameBot").createDirectory()
        val a = BotEntry.of(dir)
        val b = BotEntry.of(dir)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `directory with spaces in name works`() {
        val dir = tempDir.resolve("My Bot Dir").createDirectory()
        val entry = BotEntry.of(dir)
        assertThat(entry.path).isEqualTo(dir)
    }
}
