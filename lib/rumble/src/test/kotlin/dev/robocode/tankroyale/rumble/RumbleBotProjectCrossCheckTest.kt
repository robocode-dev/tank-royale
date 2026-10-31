package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.BotValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files

class RumbleBotProjectCrossCheckTest {

    @Test
    @Tag("Sanity")
    fun testSanity_everyActivePublicBotPassesTheBotProjectChecksWhenAskedTo() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to read the public Rumble")
        val live = GitHubCatalogSource(RumbleEnvironments.PUBLIC)
        val head = java.net.URI.create("https://raw.githubusercontent.com/robocode-dev/rumble-bots/main/bots/index.json")
            .toURL().readText()
        val catalog = Catalog.parse(live.catalogText(Catalog.parse(head).commit))
        val target = Files.createTempDirectory("rumble-cross-check")

        try {
            val placed = BotFetcher.fetch(live, catalog.commit, catalog.activeBots, target)
                .filterIsInstance<FetchResult.Placed>()
            assertEquals(catalog.activeBots.size, placed.size)
            val failures = placed.mapNotNull { result ->
                val report = BotValidator.validate(result.directory)
                if (report.passes) null else "${result.bot.name}: ${report.findings.map { it.code + " " + it.file }}"
            }
            assertEquals(emptyList<String>(), failures)
        } finally {
            target.toFile().deleteRecursively()
        }
    }
}
