package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.runner.BattleException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

/**
 * Unit tests for [BooterManager] configuration and bot path validation.
 * These tests do NOT start real Booter processes.
 */
class BooterManagerTest {

    @TempDir
    lateinit var tempDir: Path

    // -------------------------------------------------------------------------------------
    // Bot path validation (5.4)
    // -------------------------------------------------------------------------------------

    @Test
    fun `validateBotDir accepts directory with matching json config`() {
        val botDir = tempDir.resolve("MyBot").createDirectory()
        botDir.resolve("MyBot.json").createFile()

        // Should not throw
        BooterManager.validateBotDir(botDir)
    }

    @Test
    fun `validateBotDir rejects non-directory path`() {
        val filePath = tempDir.resolve("not-a-dir.txt").createFile()

        assertThatThrownBy { BooterManager.validateBotDir(filePath) }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("not a directory")
    }

    @Test
    fun `validateBotDir rejects directory without config file`() {
        val botDir = tempDir.resolve("EmptyBot").createDirectory()

        assertThatThrownBy { BooterManager.validateBotDir(botDir) }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("configuration file")
    }

    @Test
    fun `validateBotDir rejects nonexistent path`() {
        val noSuchDir = tempDir.resolve("NoSuchBot")

        assertThatThrownBy { BooterManager.validateBotDir(noSuchDir) }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("not a directory")
    }

    // -------------------------------------------------------------------------------------
    // Construction and initial state
    // -------------------------------------------------------------------------------------

    @Test
    fun `new instance is not running`() {
        val manager = BooterManager("ws://localhost:7654", "secret")
        assertThat(manager.isRunning).isFalse()
    }

    @Test
    fun `new instance has no booted bots`() {
        val manager = BooterManager("ws://localhost:7654", "secret")
        assertThat(manager.botPids).isEmpty()
    }

    @Test
    fun `close on unstarted instance is a no-op`() {
        val manager = BooterManager("ws://localhost:7654", "secret")
        // Should not throw
        manager.close()
    }
}
