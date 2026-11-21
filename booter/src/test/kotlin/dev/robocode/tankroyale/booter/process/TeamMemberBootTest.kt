package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** BFD-008: the booter starts a team's members whether the team names them by folder or by name and version. */
class TeamMemberBootTest {

    @TempDir
    lateinit var dir: Path

    private val started = mutableListOf<Process>()

    private val json = Json { ignoreUnknownKeys = true }
    private val java = File(System.getProperty("java.home"), "bin/java").absolutePath

    /** A booter whose "bot" is a Java process that ends at once, started from a script every bot folder is said to have. */
    private fun booter() = BotBooter(
        launchProcess = { ProcessBuilder(java, "-version") },
        setupEnvironment = { _, _, _ -> },
        findScript = { botDir -> botDir.resolve("start.sh") },
    )

    private fun entry(folder: Path): BootEntry? {
        val file = folder.resolve("${folder.fileName}.json")
        return if (Files.isRegularFile(file)) json.decodeFromString(BootEntry.serializer(), Files.readString(file)) else null
    }

    private fun bot(folder: String, name: String = folder, version: String = "1.0.0"): Path {
        val bot = Files.createDirectories(dir.resolve(folder))
        Files.writeString(bot.resolve("$folder.json"), """{ "name": "$name", "version": "$version", "authors": ["A"] }""")
        Files.writeString(bot.resolve("start.sh"), "")
        return bot
    }

    private fun team(members: List<String>): Path {
        val team = Files.createDirectories(dir.resolve("Pair"))
        Files.writeString(team.resolve("Pair.json"), """{ "name": "Pair", "version": "1.0.0", "authors": ["A"], "teamMembers": [${members.joinToString { "\"$it\"" }}] }""")
        return team
    }

    private fun boot(team: Path): Set<String> {
        val processes = booter().boot(team, ::entry)
        started += processes.keys
        return processes.values.map { it.fileName.toString() }.toSet()
    }

    @AfterEach
    fun endProcesses() {
        started.forEach { it.destroyForcibly(); it.waitFor() }
    }

    @Test
    @Tag("BFD-008")
    @Tag("Unit")
    @Tag("Positive")
    fun testBFD008_UnitPositive_membersNamedByNameAndVersionOrByFolderAreStartedFromTheirFolders() {
        bot("Corners")
        bot("Crazy")

        assertThat(boot(team(listOf("Corners 1.0.0", "Crazy")))).containsExactlyInAnyOrder("Corners", "Crazy")
    }

    @Test
    @Tag("BFD-008")
    @Tag("Unit")
    @Tag("Negative")
    fun testBFD008_UnitNegative_aMemberFoundInTwoFoldersOrInNoneIsNotStarted() {
        bot("Corners")
        bot("CornersCopy", name = "Corners")
        bot("Crazy")

        assertThat(boot(team(listOf("Corners 1.0.0", "Crazy 1.0.0", "Nobody 9.9")))).containsExactly("Crazy")
    }
}
