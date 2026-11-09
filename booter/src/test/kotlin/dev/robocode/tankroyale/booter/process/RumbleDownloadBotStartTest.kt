package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.commands.DirCommand
import dev.robocode.tankroyale.common.RUMBLE_PYTHON_ENVIRONMENT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Bots downloaded from the Rumble start with their folder's private Python environment, and nothing else changes. */
class RumbleDownloadBotStartTest {

    @TempDir
    lateinit var dir: Path

    private fun environment(folder: Path, windows: Boolean): Path {
        val scripts = Files.createDirectories(folder.resolve(RUMBLE_PYTHON_ENVIRONMENT).resolve(if (windows) "Scripts" else "bin"))
        return Files.write(scripts.resolve(if (windows) "python.exe" else "python"), ByteArray(0))
    }

    private fun bot(folder: Path, name: String): Path {
        val bot = Files.createDirectories(folder.resolve(name))
        Files.writeString(bot.resolve("$name.json"), """{ "name": "$name", "version": "1.0", "authors": ["A"] }""")
        return bot
    }

    @Test
    @Tag("RCL-086")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL086_UnitPositive_aBotInAFolderWithTheEnvironmentStartsWithItFirstOnItsPath() {
        listOf(true, false).forEach { windows ->
            val folder = Files.createDirectories(dir.resolve(if (windows) "windows" else "posix"))
            val python = environment(folder, windows)
            val env = mutableMapOf<String, String?>("Path" to "C:\\tools", "BOT_NAME" to "Orbit")

            DownloadedBotsPython.apply(env, bot(folder, "Orbit"), windows)

            val separator = if (windows) ";" else ":"
            assertThat(env["Path"]).isEqualTo(python.parent.toString() + separator + "C:\\tools")
            assertThat(env).doesNotContainKey("PATH")
            assertThat(env["RUMBLE_PYTHON"]).isEqualTo(python.toString())
            assertThat(env["BOT_NAME"]).isEqualTo("Orbit")
        }
    }

    @Test
    @Tag("RCL-086")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL086_UnitNegative_aBotInAnyOtherFolderStartsWithItsEnvironmentUnchanged() {
        val mine = Files.createDirectories(dir.resolve("mine"))
        Files.createDirectories(mine.resolve(RUMBLE_PYTHON_ENVIRONMENT))
        val env = mutableMapOf<String, String?>("PATH" to "/usr/bin", "BOT_NAME" to "Walls")
        val before = env.toMap()

        DownloadedBotsPython.apply(env, bot(mine, "Walls"), windows = false)
        DownloadedBotsPython.apply(env, bot(Files.createDirectories(dir.resolve("other")), "Walls"), windows = true)

        assertThat(env).isEqualTo(before)
    }

    @Test
    @Tag("RCL-085")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL085_UnitNegative_theEnvironmentFolderIsNeverListedAsABot() {
        environment(dir, windows = true)
        environment(dir, windows = false)
        Files.writeString(dir.resolve(RUMBLE_PYTHON_ENVIRONMENT).resolve("pyvenv.cfg"), "home = /usr/bin\n")
        Files.writeString(dir.resolve(RUMBLE_PYTHON_ENVIRONMENT).resolve("$RUMBLE_PYTHON_ENVIRONMENT.py"), "print('x')\n")
        bot(dir, "Orbit")

        val listed = DirCommand(listOf(dir)).listBootEntries(null, botsOnly = false, teamsOnly = false).map { it.name }

        assertThat(listed).containsExactly("Orbit")
    }
}
