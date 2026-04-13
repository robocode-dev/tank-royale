package dev.robocode.tankroyale.gui.booter

import dev.robocode.tankroyale.client.model.BotInfo

/**
 * Matches connected [BotInfo] instances against an expected multiset of [BotIdentity],
 * plus a count of bots whose identity is unknown at boot time (no .json file).
 *
 * Specific identities are matched by name+version.
 * Unknown slots are filled by any newly connected bot not in the [baseline] and not
 * already consumed by a specific-identity match.
 */
class BotMatcher(
    expectedIdentities: List<BotIdentity>,
    val unknownCount: Int = 0,
    private val baseline: Map<BotIdentity, Int> = emptyMap(),
) {

    /** Expected count per identity. */
    val expected: Map<BotIdentity, Int> = expectedIdentities
        .groupingBy { it }
        .eachCount()

    /** Connected count per identity (updated via [update]). */
    var connected: Map<BotIdentity, Int> = emptyMap()
        private set

    /** Number of unknown slots filled by new bots since boot. */
    var unknownConnected: Int = 0
        private set

    /** True when every expected identity slot is filled and all unknown slots are filled. */
    val isComplete: Boolean
        get() = expected.all { (identity, count) -> (connected[identity] ?: 0) >= count }
                && unknownConnected >= unknownCount

    /** Pending specific identities (those not yet fully connected). */
    val pending: Map<BotIdentity, Int>
        get() = expected.mapValues { (identity, count) ->
            maxOf(0, count - (connected[identity] ?: 0))
        }.filter { it.value > 0 }

    /** Number of unknown slots not yet filled. */
    val unknownPending: Int
        get() = maxOf(0, unknownCount - unknownConnected)

    /**
     * Updates the connected multiset from the current list of connected bots.
     * Specific-identity slots are matched by name+version.
     * Unknown slots are filled by bots not in [baseline] and not consumed by specific matches.
     */
    fun update(bots: Collection<BotInfo>) {
        val allIdentities = bots.map { BotIdentity(it.name, it.version) }

        connected = allIdentities
            .filter { it in expected }
            .groupingBy { it }
            .eachCount()

        // New bots = excess over baseline count, excluding specific-identity matches
        val counts = allIdentities.groupingBy { it }.eachCount()
        unknownConnected = counts
            .filter { (id, _) -> id !in expected }
            .entries.sumOf { (id, count) -> maxOf(0, count - (baseline[id] ?: 0)) }
            .coerceAtMost(unknownCount)
    }
}
