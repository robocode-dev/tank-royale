package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.client.model.BotAddress
import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.runner.BotIdentity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BotMatcherTest {

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------

    private fun botInfo(name: String, version: String, host: String, port: Int = 7654, authors: String = "Author") =
        BotInfo(
            name = name,
            version = version,
            authors = listOf(authors),
            countryCodes = listOf("US"),
            gameTypes = setOf("classic"),
            host = host,
            port = port,
        )

    private fun identity(name: String, version: String = "1.0", authors: String = "Author") = BotIdentity(name, version, authors)
    private fun address(host: String, port: Int = 7654) = BotAddress(host, port)

    // -------------------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------------------

    @Test
    fun `2 distinct bots both connect - isComplete true and matched has 2 addresses`() {
        val matcher = BotMatcher(
            expectedIdentities = listOf(identity("BotA"), identity("BotB")),
            preExistingBots = emptySet(),
        )

        val bots = setOf(
            botInfo("BotA", "1.0", "host-a"),
            botInfo("BotB", "1.0", "host-b"),
        )
        val result = matcher.update(bots)

        assertThat(result.isComplete).isTrue()
        assertThat(result.matched).containsExactlyInAnyOrder(address("host-a"), address("host-b"))
    }

    @Test
    fun `same bot directory twice - needs 2 connections with same name and version`() {
        val matcher = BotMatcher(
            expectedIdentities = listOf(identity("Drone"), identity("Drone")),
            preExistingBots = emptySet(),
        )

        // Only one connected — not complete
        val oneBot = setOf(botInfo("Drone", "1.0", "host-1"))
        val partial = matcher.update(oneBot)
        assertThat(partial.isComplete).isFalse()

        // Two connected — complete
        val twoBots = setOf(
            botInfo("Drone", "1.0", "host-1"),
            botInfo("Drone", "1.0", "host-2", port = 7655),
        )
        val full = matcher.update(twoBots)
        assertThat(full.isComplete).isTrue()
        assertThat(full.matched).hasSize(2)
    }

    @Test
    fun `team with 4 droids same identity - needs 4 connections`() {
        val matcher = BotMatcher(
            expectedIdentities = listOf(
                identity("Droid"), identity("Droid"), identity("Droid"), identity("Droid")
            ),
            preExistingBots = emptySet(),
        )

        val bots = (1..4).map { botInfo("Droid", "1.0", "host-$it", port = 7650 + it) }.toSet()
        val result = matcher.update(bots)

        assertThat(result.isComplete).isTrue()
        assertThat(result.matched).hasSize(4)
    }

    @Test
    fun `stray bot with different identity - filtered out and does not count toward match`() {
        val matcher = BotMatcher(
            expectedIdentities = listOf(identity("BotA")),
            preExistingBots = emptySet(),
        )

        val bots = setOf(
            botInfo("BotA", "1.0", "host-a"),
            botInfo("StrayBot", "1.0", "host-stray"),
        )
        val result = matcher.update(bots)

        assertThat(result.isComplete).isTrue()
        assertThat(result.matched).containsExactly(address("host-a"))
        assertThat(result.matched).doesNotContain(address("host-stray"))
    }

    @Test
    fun `pre-existing bots are excluded from matching`() {
        val preExisting = setOf(address("host-pre"))
        val matcher = BotMatcher(
            expectedIdentities = listOf(identity("BotA")),
            preExistingBots = preExisting,
        )

        // Pre-existing bot has same identity as expected — should NOT count
        val bots = setOf(botInfo("BotA", "1.0", "host-pre"))
        val result = matcher.update(bots)

        assertThat(result.isComplete).isFalse()
        assertThat(result.matched).isEmpty()
        assertThat(result.pending).containsKey(identity("BotA"))
    }

    @Test
    fun `partial connection 1 of 2 - isComplete false and pending shows missing identity`() {
        val matcher = BotMatcher(
            expectedIdentities = listOf(identity("BotA"), identity("BotB")),
            preExistingBots = emptySet(),
        )

        val bots = setOf(botInfo("BotA", "1.0", "host-a"))
        val result = matcher.update(bots)

        assertThat(result.isComplete).isFalse()
        assertThat(result.connected).containsKey(identity("BotA"))
        assertThat(result.pending).containsKey(identity("BotB"))
        assertThat(result.pending[identity("BotB")]).isEqualTo(1)
    }

    @Test
    fun `extra bots beyond expected count - only expected count taken and isComplete true`() {
        val matcher = BotMatcher(
            expectedIdentities = listOf(identity("BotA")),
            preExistingBots = emptySet(),
        )

        // 3 bots with same identity, only 1 expected
        val bots = setOf(
            botInfo("BotA", "1.0", "host-1"),
            botInfo("BotA", "1.0", "host-2", port = 7655),
            botInfo("BotA", "1.0", "host-3", port = 7656),
        )
        val result = matcher.update(bots)

        assertThat(result.isComplete).isTrue()
        assertThat(result.matched).hasSize(1)
    }
}
