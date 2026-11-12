package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission
import java.math.BigInteger

/**
 * One thing the pre-check found: a stable [code], what it means in plain words and the [fix] for it, and the [values]
 * that the message names, in order, so a screen can say it in its own words and languages.
 */
data class PrecheckFinding(val code: String, val message: String, val fix: String, val values: List<String> = emptyList())

/**
 * What the pre-check found. [problems] are things the Rumble's own validator would refuse; [warnings] are things it
 * would accept that the author probably does not intend. The report only advises: nothing in the sending depends on it.
 */
data class PrecheckReport(val problems: List<PrecheckFinding>, val warnings: List<PrecheckFinding>) {
    val hasProblems: Boolean get() = problems.isNotEmpty()
}

/**
 * Tells an author, before anything becomes public, which of the bot repository's per-bot rules a staged bot would
 * fail: a banned account, a disqualified bot, a name that belongs to another owner, and a version that is already
 * published. It mirrors only what the repository's validator enforces on a single bot.
 */
object SubmissionPrecheck {
    const val ACCOUNT_BANNED = "account-banned"
    const val BOT_DISQUALIFIED = "bot-disqualified"
    const val OWNED_BY_ANOTHER = "owned-by-another"
    const val SAME_VERSION_DIFFERENT_SOURCE = "same-version-different-source"
    const val ALREADY_PUBLISHED = "already-published"
    const val LOWER_VERSION = "lower-version"

    /**
     * The problems that stop a send in the desktop program: who may submit which name. A version already published is
     * stopped by the content check, which reads the same catalog.
     */
    val STOPPING: Set<String> = setOf(ACCOUNT_BANNED, BOT_DISQUALIFIED, OWNED_BY_ANOTHER)

    private val PLAIN_VERSION = Regex("\\d+(?:\\.\\d+)*")

    fun check(submission: StagedSubmission, account: String, catalog: Catalog, rules: RumbleRules): PrecheckReport {
        val name = submission.name
        val version = submission.version
        val problems = mutableListOf<PrecheckFinding>()
        val warnings = mutableListOf<PrecheckFinding>()

        if (rules.bannedAccounts.any { it.equals(account, ignoreCase = true) }) {
            problems += PrecheckFinding(
                ACCOUNT_BANNED, "The account $account is banned from submitting to this Rumble.",
                "Contact the Rumble's moderators if you think this is a mistake.", listOf(account),
            )
        }
        if (name in rules.disqualifiedBots) {
            problems += PrecheckFinding(
                BOT_DISQUALIFIED, "A bot called $name has been disqualified from this Rumble.",
                "Choose another name for your bot, or contact the moderators.", listOf(name),
            )
        }
        val owner = rules.ownerOf(name)
        if (owner != null && owner.accounts.none { it.equals(account, ignoreCase = true) }) {
            problems += PrecheckFinding(
                OWNED_BY_ANOTHER, "The name $name belongs to another owner, ${owner.ownerId}.",
                "Choose another name for your bot.", listOf(name, owner.ownerId),
            )
        }
        val active = catalog.activeBots.firstOrNull { it.name == name }
        if (active != null && version != null) {
            if (active.version == version) {
                if (active.sourceHash == submission.treeHash) {
                    problems += PrecheckFinding(
                        ALREADY_PUBLISHED, "Version $version of $name is already in the Rumble with exactly these files.",
                        "There is nothing to send. Change the bot and raise its version to send a new one.", listOf(version, name),
                    )
                } else {
                    problems += PrecheckFinding(
                        SAME_VERSION_DIFFERENT_SOURCE,
                        "Version $version of $name is already in the Rumble, and your files differ from it.",
                        "Raise the version in the bot's configuration file, then stage it again.", listOf(version, name),
                    )
                }
            } else if (isLower(version, active.version)) {
                warnings += PrecheckFinding(
                    LOWER_VERSION,
                    "Version $version is lower than the version ${active.version} that is active now. " +
                        "The Rumble accepts it, but the older version would replace the newer.",
                    "If that is not what you want, raise the version above ${active.version}.", listOf(version, active.version),
                )
            }
        }
        return PrecheckReport(problems, warnings)
    }

    /** True only when both are plain dotted numbers and [version] is lower than [other]. */
    private fun isLower(version: String, other: String): Boolean {
        if (!PLAIN_VERSION.matches(version) || !PLAIN_VERSION.matches(other)) return false
        val a = version.split('.').map { it.toBigInteger() }
        val b = other.split('.').map { it.toBigInteger() }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { BigInteger.ZERO }
            val y = b.getOrElse(i) { BigInteger.ZERO }
            if (x != y) return x < y
        }
        return false
    }
}
