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
    private val expectedIdentities: List<BotIdentity>,
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
     * Filters out pre-existing bots, builds a connected multiset from `BotInfo.name`/`BotInfo.version`,
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

        val matched = linkedSetOf<BotAddress>()
        val connected = mutableMapOf<BotIdentity, Int>()
        val pending = mutableMapOf<BotIdentity, Int>()

        // Match one connection per expected slot in the original roster order. Grouping only by
        // identity would reorder mixed teams and change the server's duplicate-name suffix order.
        val matchedByIdentity = mutableMapOf<BotIdentity, Int>()
        for (identity in expectedIdentities) {
            val bot = candidates.firstOrNull { candidate ->
                candidate.botAddress !in matched && candidate.matches(identity)
            } ?: continue
            matched.add(bot.botAddress)
            matchedByIdentity[identity] = (matchedByIdentity[identity] ?: 0) + 1
        }

        for ((identity, needed) in expectedMultiset) {
            val taken = matchedByIdentity[identity] ?: 0

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

    private fun BotInfo.matches(identity: BotIdentity): Boolean =
        name == identity.name && version == identity.version && authors.joinToString(", ") == identity.authors
}
