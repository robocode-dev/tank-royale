package dev.robocode.tankroyale.booter.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class DirCommandTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    @Tag("BFD-006")
    fun `BFD-006 Positive and negative directory listing retains optional license`() {
        createBot("LicensedBot", "MIT")
        val unlicensedBot = createBot("UnlicensedBot", null)

        val entries = DirCommand(listOf(tempDir)).listBootEntries(null, botsOnly = true, teamsOnly = false)

        assertThat(entries).anyMatch { it.name == "LicensedBot" && it.license == "MIT" }
        assertThat(entries).anyMatch { it.name == "UnlicensedBot" && it.license == null }
        assertThat(unlicensedBot).exists()
    }

    private fun createBot(name: String, license: String?): Path {
        val directory = Files.createDirectory(tempDir.resolve(name))
        val licenseJson = license?.let { "\n  \"license\": \"$it\"," } ?: ""
        Files.writeString(
            directory.resolve("$name.json"),
            """
            {
              "name": "$name",
              "version": "1.0",
              "authors": ["Test Author"],$licenseJson
              "gameTypes": ["classic"]
            }
            """.trimIndent(),
        )
        return directory
    }
}
