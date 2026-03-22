package dev.robocode.tankroyale.gui.booter

import dev.robocode.tankroyale.client.model.BotInfo

/**
 * Matches connected [BotInfo] instances against an expected multiset of [BotIdentity].
 *
 * Stray bots (identities not in the expected set) are ignored.
 */
class BotMatcher(expectedIdentities: List<BotIdentity>) {

    /** Expected count per identity. */
    val expected: Map<BotIdentity, Int> = expectedIdentities
        .groupingBy { it }
        .eachCount()

    /** Connected count per identity (updated via [update]). */
    var connected: Map<BotIdentity, Int> = emptyMap()
        private set

    /** True when every expected identity slot is filled. */
    val isComplete: Boolean
        get() = expected.all { (identity, count) -> (connected[identity] ?: 0) >= count }

    /** Pending identities (those not yet fully connected). */
    val pending: Map<BotIdentity, Int>
        get() = expected.mapValues { (identity, count) ->
            maxOf(0, count - (connected[identity] ?: 0))
        }.filter { it.value > 0 }

    /**
     * Updates the connected multiset from the current list of connected bots.
     * Only bots whose identity is in [expected] are counted.
     */
    fun update(bots: Collection<BotInfo>) {
        connected = bots
            .map { BotIdentity(it.name, it.version) }
            .filter { it in expected }
            .groupingBy { it }
            .eachCount()
    }
}
