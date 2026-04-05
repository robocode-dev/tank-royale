package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.client.model.BotAddress
import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.runner.BotIdentity

/**
 * Matches a set of connected bots against an expected list of [BotIdentity] instances,
 * supporting duplicate identities (multiset semantics).
 *
 * Pre-existing bots (connected before the battle started) are excluded from matching.
 *
 * @param expectedIdentities the list of bot identities required for the battle to start,
 *   including duplicates (e.g., two entries of the same identity means two connections needed)
 * @param preExistingBots bots that were already connected before this battle; excluded from matching
 */
internal class BotMatcher(
    expectedIdentities: List<BotIdentity>,
    private val preExistingBots: Set<BotAddress>,
    private val expectedBotCount: Int = expectedIdentities.size,
) {
    /** Expected identity counts (multiset). */
    val expectedMultiset: Map<BotIdentity, Int> = expectedIdentities
        .groupingBy { it }
        .eachCount()

    /**
     * Result of a matching attempt against the currently connected bots.
     *
     * @property matched the set of [BotAddress] instances that satisfy the expected identities
     * @property isComplete true when all expected identity slots are filled
     * @property connected multiset of identities currently connected (capped at expected count)
     * @property pending multiset of identities still needed to complete the match
     */
    data class MatchResult(
        val matched: Set<BotAddress>,
        val isComplete: Boolean,
        val connected: Map<BotIdentity, Int>,
        val pending: Map<BotIdentity, Int>,
    )

    /**
     * Updates the match state based on the current set of connected bots.
     *
     * Filters out pre-existing bots, builds a connected multiset from [BotInfo.name]/[BotInfo.version],
     * and compares against the expected multiset. If more bots than expected connect for an identity,
     * only the needed count (first seen) is taken.
     *
     * If [expectedMultiset] is empty, it falls back to count-based matching.
     *
     * @param bots the full set of currently connected bots reported by the server
     * @return a [MatchResult] describing the current match state
     */
    fun update(bots: Set<BotInfo>): MatchResult {
        // Filter out pre-existing bots
        val candidates = bots.filter { it.botAddress !in preExistingBots }

        if (expectedMultiset.isEmpty()) {
            // Fallback for config-less bots: wait for the expected number of new bots
            val matched = candidates.take(expectedBotCount).map { it.botAddress }.toSet()
            val isComplete = matched.size >= expectedBotCount
            return MatchResult(
                matched = matched,
                isComplete = isComplete,
                connected = emptyMap(),
                pending = if (isComplete) emptyMap() else mapOf(BotIdentity("Unknown", "Unknown", "Unknown") to (expectedBotCount - matched.size))
            )
        }

        val matched = mutableSetOf<BotAddress>()
        val connected = mutableMapOf<BotIdentity, Int>()
        val pending = mutableMapOf<BotIdentity, Int>()

        for ((identity, needed) in expectedMultiset) {
            // Find candidate bots matching this identity
            val matchingBots = candidates.filter { bot ->
                bot.name == identity.name && bot.version == identity.version && bot.authors.joinToString(", ") == identity.authors
            }
            val taken = minOf(matchingBots.size, needed)
            matchingBots.take(taken).forEach { matched.add(it.botAddress) }

            if (taken > 0) connected[identity] = taken
            val stillNeeded = needed - taken
            if (stillNeeded > 0) pending[identity] = stillNeeded
        }

        return MatchResult(
            matched = matched,
            isComplete = pending.isEmpty(),
            connected = connected,
            pending = pending,
        )
    }
}
