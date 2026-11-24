package dev.robocode.tankroyale.gui.ui.rumble

import dev.robocode.tankroyale.rumble.ContributeTexts
import io.kotest.core.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
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

    "testRCL161_UnitPositive_theScreenSaysHowManyBotsHaveConnectedAndHowLongItHasWaitedInEveryLanguage" {
        for (language in listOf("", "_da", "_es", "_ca")) {
            val messages = Properties().apply { File(resources.toFile(), "Messages$language.properties").reader(Charsets.UTF_8).use { load(it) } }
            val line = messages.getProperty("rumble.contribute.starting_bots")
            val bar = messages.getProperty("rumble.contribute.starting_bots_bar")
            // Connected, expected, seconds waited and the limit in seconds; and connected and expected on the bar.
            (Regex("%d").findAll(line).count() == 4 && Regex("%d").findAll(bar).count() == 2) shouldBe true
            ('—' in line + bar || '–' in line + bar) shouldBe false
        }
    }

    "testRCL161_UnitNegative_theScreenLooksForTheStartingBotsOnlyInTheBattleThatRunsAndNotInAnOlderStatus" {
        val sources = listOf(Path.of("src", "main", "kotlin"), Path.of("gui", "src", "main", "kotlin")).first { Files.isDirectory(it) }
            .resolve("dev/robocode/tankroyale/gui/ui/rumble/RumbleContributeDialog.kt").toFile().readText()
        // The progress is drawn from the status's own field inside the running battle's block, so a status without it draws nothing.
        sources.contains("status.boot?.let") shouldBe true
        sources.indexOf("status.boot?.let") shouldBeGreaterThan sources.indexOf("status.battle?.let")
    }
})
