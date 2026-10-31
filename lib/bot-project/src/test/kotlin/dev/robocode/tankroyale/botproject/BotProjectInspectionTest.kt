package dev.robocode.tankroyale.botproject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class BotProjectInspectionTest {

    @Test
    @Tag("RBC-013")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC013_UnitPositive_aBotFolderIsReadAndItsLanguageComesFromTheDeclaredPlatform(@TempDir parent: Path) {
        val expectedLanguage = mapOf("java" to "java", "csharp" to "csharp", "python" to "python", "typescript" to "typescript")
        Fixtures.languages.forEach { language ->
            val project = (BotProjects.inspect(Fixtures.bot(parent.resolve(language), language)) as Inspection.Found).project

            assertEquals("MyBot", project.name)
            assertEquals("1.0.0", project.version)
            assertEquals(listOf("A. Author"), project.authors)
            assertEquals("MIT", project.license)
            assertEquals("A test bot", project.description)
            assertEquals(expectedLanguage[language], project.language, language)
            assertFalse(project.isTeam)
        }
    }

    @Test
    @Tag("RBC-013")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC013_UnitPositive_aTeamFolderIsToldApartFromAnIndividualBot(@TempDir parent: Path) {
        val project = (BotProjects.inspect(Fixtures.team(parent)) as Inspection.Found).project

        assertTrue(project.isTeam)
        assertEquals(listOf("MyBot 1.0.0", "MyBot 1.0.0"), project.teamMembers)
        assertNull(project.language)
    }

    @Test
    @Tag("RBC-013")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC013_UnitNegative_aFolderWithoutAReadableConfigurationFileIsNotABotProject(@TempDir parent: Path) {
        val empty = parent.resolve("Empty").also { Fixtures.write(it.resolve("readme.txt"), "x") }
        val missing = assertInstanceOf(Inspection.NotAProject::class.java, BotProjects.inspect(empty))
        assertEquals("MISSING_CONFIG", missing.code)
        assertTrue(missing.reason.contains("Empty.json"), missing.reason)

        val broken = parent.resolve("Broken").also { Fixtures.write(it.resolve("Broken.json"), "{ not json") }
        assertEquals("INVALID_CONFIG", (BotProjects.inspect(broken) as Inspection.NotAProject).code)

        val list = parent.resolve("Listy").also { Fixtures.write(it.resolve("Listy.json"), "[1, 2]") }
        assertEquals("INVALID_CONFIG", (BotProjects.inspect(list) as Inspection.NotAProject).code)

        val file = parent.resolve("Empty").resolve("readme.txt")
        assertEquals("NOT_A_FOLDER", (BotProjects.inspect(file) as Inspection.NotAProject).code)
    }
}
