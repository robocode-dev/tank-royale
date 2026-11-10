package dev.robocode.tankroyale.rumble

/** A bot as it names itself: its name, version and authors. */
data class BotIdentity(val name: String, val version: String, val authors: List<String>) {
    internal val key: Triple<String, String, Set<String>> = Triple(name.trim(), version.trim(), authors.map { it.trim() }.filter { it.isNotEmpty() }.toSet())
}

/**
 * The licenses the bots in the bot directories declare, so a bot that joined a battle on its own, which brings no license,
 * can be shown the license of its own folder. Nothing here touches Swing or the disk.
 */
class KnownLicenses(bots: List<Pair<BotIdentity, String?>>) {
    private val licenses: Map<Triple<String, String, Set<String>>, Set<String?>> =
        bots.groupBy({ it.first.key }, { it.second?.trim() }).mapValues { it.value.toSet() }

    /**
     * The license of the bot in the bot directories with the same name, version and authors as [bot]: empty when that bot
     * declares none, and null when there is no such bot, or when several such bots declare different licenses, so the
     * license of another bot is never shown.
     */
    fun of(bot: BotIdentity): String? {
        val declared = licenses[bot.key]?.map { it.orEmpty() }?.toSet() ?: return null
        return declared.singleOrNull()
    }
}
