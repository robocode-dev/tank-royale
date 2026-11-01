package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.BotStaging
import dev.robocode.tankroyale.botproject.BotValidator
import dev.robocode.tankroyale.botproject.StagingResult
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

    @Test
    @Tag("RBC-024")
    @Tag("Integration")
    @Tag("Positive")
    fun testRBC024_IntegrationPositive_everyActivePublicBotStagesToTheHashTheCatalogRecorded() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to read the public Rumble")
        val live = GitHubCatalogSource(RumbleEnvironments.PUBLIC)
        val head = java.net.URI.create("https://raw.githubusercontent.com/robocode-dev/rumble-bots/main/bots/index.json")
            .toURL().readText()
        val catalog = Catalog.parse(live.catalogText(Catalog.parse(head).commit))
        val fetched = Files.createTempDirectory("rumble-stage-fetched")
        val staging = Files.createTempDirectory("rumble-stage-copy")

        try {
            val placed = BotFetcher.fetch(live, catalog.commit, catalog.activeBots, fetched).filterIsInstance<FetchResult.Placed>()
            assertEquals(catalog.activeBots.size, placed.size)
            val problems = placed.mapNotNull { result ->
                val folder = staging.resolve(result.bot.name)
                val language = result.bot.path.split('/')[1]
                when (val staged = BotStaging.stage(result.directory, folder, teamLanguage = language)) {
                    is StagingResult.Staged ->
                        if (staged.submission.treeHash == result.bot.sourceHash) null
                        else "${result.bot.name}: staged hash ${staged.submission.treeHash} differs from ${result.bot.sourceHash}"
                    is StagingResult.Rejected -> "${result.bot.name}: rejected ${staged.findings.map { it.code }}"
                    is StagingResult.Refused -> "${result.bot.name}: refused ${staged.message}"
                }
            }
            assertEquals(emptyList<String>(), problems)
        } finally {
            fetched.toFile().deleteRecursively()
            staging.toFile().deleteRecursively()
        }
    }
}
