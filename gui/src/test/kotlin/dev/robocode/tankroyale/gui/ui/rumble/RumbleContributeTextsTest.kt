package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.rumble.ContributeTexts
import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** Every text the Contribute battles screen asks for is in the resource files, in every language. */
class RumbleContributeTextsTest : StringSpec({
    tags(Tag("Unit"))

    val resources = listOf(Path.of("src", "main", "resources"), Path.of("gui", "src", "main", "resources")).first { Files.isDirectory(it) }

    fun keys(name: String) = Properties().apply { File(resources.toFile(), name).reader(Charsets.UTF_8).use { load(it) } }.stringPropertyNames()

    // The keys the screen's own code names, read from its sources, besides the ones the library lists.
    fun keysInSources(): Set<String> {
        val sources = listOf("RumbleContribute.kt", "RumbleContributeDialog.kt").map { name ->
            listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }
                .resolve("dev/robocode/tankroyale/gui/ui/rumble/$name").toFile().readText()
        }
        return sources.flatMap { Regex("\"(rumble\\.contribute\\.[a-z_.]+)\"").findAll(it).map { m -> m.groupValues[1] }.toList() }.toSet()
    }

    "testRCL136_UnitPositive_everyTextTheScreenShowsIsInEveryLanguage" {
        val wanted = ContributeTexts.allKeys().toSet() + keysInSources()
        for (language in listOf("", "_da", "_es", "_ca")) {
            (wanted - keys("Messages$language.properties")).shouldBeEmpty()
        }
    }

    "testRCL136_UnitNegative_anOutcomeTheScreenDoesNotKnowStillHasAText" {
        val messages = keys("Messages.properties")
        listOf("something-new", "", "finished-later").map { ContributeTexts.forOutcome(it) }.filter { it !in messages }.shouldBeEmpty()
        listOf("UNKNOWN", null).map { ContributeTexts.forWaiting(it) }.filter { it !in messages }.shouldBeEmpty()
    }
})
