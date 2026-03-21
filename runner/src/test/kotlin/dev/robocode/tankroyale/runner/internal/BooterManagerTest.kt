package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.runner.BattleException
import dev.robocode.tankroyale.runner.BotIdentity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.writeText

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

    // -------------------------------------------------------------------------------------
    // captureOutput parameter
    // -------------------------------------------------------------------------------------

    @Test
    fun `captureOutput false is accepted without throwing`() {
        val manager = BooterManager("ws://localhost:7654", "secret", captureOutput = false)
        assertThat(manager.isRunning).isFalse()
        assertThat(manager.botPids).isEmpty()
    }

    @Test
    fun `captureOutput true is the default`() {
        val manager = BooterManager("ws://localhost:7654", "secret")
        assertThat(manager.isRunning).isFalse()
    }

    // -------------------------------------------------------------------------------------
    // readBotIdentities (1.2 / 1.3)
    // -------------------------------------------------------------------------------------

    private fun createBotDir(parent: Path, dirName: String, name: String, version: String): Path {
        val botDir = parent.resolve(dirName).createDirectory()
        botDir.resolve("$dirName.json").writeText("""{"name":"$name","version":"$version"}""")
        return botDir
    }

    @Test
    fun `readBotIdentities returns single identity for regular bot directory`() {
        val botDir = createBotDir(tempDir, "MyFirstBot", "My First Bot", "1.0")

        val identities = BooterManager.readBotIdentities(botDir)

        assertThat(identities).containsExactly(BotIdentity("My First Bot", "1.0"))
    }

    @Test
    fun `readBotIdentities returns one identity per team member including duplicates`() {
        // Create 4 distinct member bots plus 1 duplicate (Drone appears twice)
        createBotDir(tempDir, "Drone", "Drone", "1.0")
        createBotDir(tempDir, "Scout", "Scout", "1.0")
        createBotDir(tempDir, "Tank", "Tank", "1.0")
        createBotDir(tempDir, "Support", "Support", "1.0")

        val teamDir = tempDir.resolve("MyTeam").createDirectory()
        teamDir.resolve("MyTeam.json").writeText(
            """{"name":"MyTeam","version":"1.0","teamMembers":["Drone","Scout","Tank","Support","Drone"]}"""
        )

        val identities = BooterManager.readBotIdentities(teamDir)

        assertThat(identities).hasSize(5)
        assertThat(identities.count { it.name == "Drone" }).isEqualTo(2)
    }

    @Test
    fun `readBotIdentities throws BattleException when bot json is missing`() {
        val botDir = tempDir.resolve("MissingBot").createDirectory()
        // No JSON file created

        assertThatThrownBy { BooterManager.readBotIdentities(botDir) }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("not found")
    }

    @Test
    fun `readBotIdentities throws BattleException when name field is missing`() {
        val botDir = tempDir.resolve("NoNameBot").createDirectory()
        botDir.resolve("NoNameBot.json").writeText("""{"version":"1.0"}""")

        assertThatThrownBy { BooterManager.readBotIdentities(botDir) }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("name")
    }

    @Test
    fun `readBotIdentities throws BattleException when team member directory is missing`() {
        createBotDir(tempDir, "PresentBot", "Present Bot", "1.0")

        val teamDir = tempDir.resolve("BrokenTeam").createDirectory()
        teamDir.resolve("BrokenTeam.json").writeText(
            """{"name":"BrokenTeam","version":"1.0","teamMembers":["PresentBot","MissingBot"]}"""
        )

        assertThatThrownBy { BooterManager.readBotIdentities(teamDir) }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("not found")
    }
}
