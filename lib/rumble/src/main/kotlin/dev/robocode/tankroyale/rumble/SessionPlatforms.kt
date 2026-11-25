package dev.robocode.tankroyale.rumble

import java.nio.file.Path

/**
 * The platforms of the catalog's bots that cannot run on this computer, found before a contribution session's first
 * battle; their bots are left out of the whole session. The battle worker runs on the session program's own Java, so it
 * needs no Java on the path; Java bots do, since their launchers call `java`. Python is not judged here, because the
 * session sets up its own Python Bot API and says what it finds itself.
 */
data class SessionPlatforms(
    /** The platforms of the session's bots that cannot run here, other than Python, in the catalog's order. */
    val unavailable: List<PlatformCheck>,
    /** The bots left out because of them, as "name version", with every team that holds one. */
    val leftOut: Set<String>,
) {
    companion object {
        /**
         * Checks the platforms of [bots] with [checker], against [versions] when the Rumble's runtime list could be read.
         * [folder] is where Python would be looked for, which is not asked here.
         */
        fun check(checker: PlatformChecker, versions: SupportedVersions?, bots: List<CatalogBot>, folder: Path): SessionPlatforms {
            val platforms = bots.mapNotNull { BotPlatform.of(it.platform) }.distinct().filter { it != BotPlatform.PYTHON }
            val checks = checker.check(platforms, versions, folder)
            val unavailable = platforms.mapNotNull { checks[it] }.filter { !it.ready }
            val names = unavailable.map { it.platform }.toSet()
            val direct = bots.filter { BotPlatform.of(it.platform) in names }.map { SessionPlanner.key(it) }.toSet()
            val teams = bots.filter { team -> team.teamMembers.any { it in direct } }.map { SessionPlanner.key(it) }
            return SessionPlatforms(unavailable, direct + teams)
        }
    }
}
