package dev.robocode.tankroyale.runner

/**
 * Snapshot of bot-connection progress during the boot phase of a battle.
 *
 * Fired via [BattleHandle.onBootProgress] on every [dev.robocode.tankroyale.client.model.BotListUpdate]
 * and periodically (every 500 ms) while waiting for bots to connect.
 *
 * @property expected multiset of identities that must connect (identity → required count)
 * @property connected multiset of identities that have connected so far (identity → connected count)
 * @property pending multiset of identities still missing (identity → remaining count)
 * @property elapsedMs milliseconds elapsed since boot started
 * @property timeoutMs configured bot-connect timeout in milliseconds
 */
data class BootProgress(
    val expected: Map<BotIdentity, Int>,
    val connected: Map<BotIdentity, Int>,
    val pending: Map<BotIdentity, Int>,
    val elapsedMs: Long,
    val timeoutMs: Long,
) {
    /** Total number of bot slots that must be filled. */
    val totalExpected: Int get() = expected.values.sum()

    /** Total number of bot slots filled so far. */
    val totalConnected: Int get() = connected.values.sum()
}
