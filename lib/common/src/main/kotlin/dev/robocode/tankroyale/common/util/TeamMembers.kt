package dev.robocode.tankroyale.common.util

import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

/** Which folder a team's member entry names, as [TeamMembers.find] finds it. */
sealed interface TeamMemberMatch {
    /** The member is the bot in [folder]. */
    data class Found(val folder: Path) : TeamMemberMatch

    /** No folder beside the team is the member [entry]. */
    data class NotFound(val entry: String) : TeamMemberMatch

    /** Several folders beside the team hold a bot with the name and version [entry]; none is chosen. */
    data class Ambiguous(val entry: String, val folders: List<Path>) : TeamMemberMatch
}

/**
 * Finds the members of a team the one way every part of the engine does, as the decision that a team names each member
 * by its folder or by its name and version sets out. A member entry in a team's `teamMembers` is either the name of a
 * folder beside the team's folder or a bot's name and version joined by one space, such as `Corners 1.0.0`, which is how
 * the Rumble writes them.
 */
object TeamMembers {

    /**
     * Finds the member [entry] of the team in [teamFolder]:
     *
     * 1. A folder beside the team's folder with exactly the entry's name is the member.
     * 2. Otherwise the member is the one folder beside it whose bot's name and version, joined by one space, equal the
     *    entry exactly. [identityOf] reads a folder's bot name and version, or gives null for a folder without a bot
     *    file, which can then only be named by its folder.
     * 3. Otherwise no folder, or several, match, and nothing is guessed.
     */
    fun find(teamFolder: Path, entry: String, identityOf: (Path) -> Pair<String, String>?): TeamMemberMatch {
        val self = teamFolder.toAbsolutePath().normalize()
        val parent = self.parent ?: return TeamMemberMatch.NotFound(entry)
        // The team's own folder is never one of its members, by its folder name or by its identity.
        exactFolder(parent, entry)?.takeIf { it != self }?.let { return TeamMemberMatch.Found(it) }

        val siblings = try {
            Files.list(parent).use { paths -> paths.iterator().asSequence().filter { Files.isDirectory(it) && it.normalize() != self }.sorted().toList() }
        } catch (e: IOException) {
            return TeamMemberMatch.NotFound(entry)
        }
        val matches = siblings.filter { folder ->
            val identity = try {
                identityOf(folder)
            } catch (e: Exception) {
                null
            }
            identity != null && "${identity.first} ${identity.second}" == entry
        }
        return when (matches.size) {
            0 -> TeamMemberMatch.NotFound(entry)
            1 -> TeamMemberMatch.Found(matches.single())
            else -> TeamMemberMatch.Ambiguous(entry, matches)
        }
    }

    /** A plain sentence saying why [match] names no member, for an error or a log line; null for a member found. */
    fun problem(teamFolder: Path, match: TeamMemberMatch): String? = when (match) {
        is TeamMemberMatch.Found -> null
        is TeamMemberMatch.NotFound ->
            "Team member not found: ${match.entry}. No folder beside ${teamFolder.toAbsolutePath().normalize()} is named so, " +
                "and none holds a bot with that name and version."
        is TeamMemberMatch.Ambiguous ->
            "Team member ${match.entry} is ambiguous: these folders beside the team all hold it: " +
                match.folders.joinToString { it.fileName.toString() } + ". Remove or rename all but one."
    }

    private fun exactFolder(parent: Path, entry: String): Path? {
        if (entry.isEmpty()) return null
        val candidate = try {
            parent.resolve(entry)
        } catch (e: InvalidPathException) {
            return null
        }
        // Only a folder directly beside the team counts, never one reached through a path inside the entry.
        val folder = candidate.normalize()
        // A trailing separator, as in a hand-written `Corners/`, still names the folder `Corners`.
        if (folder.parent != parent.normalize() || folder.fileName.toString() != entry.trimEnd('/', '\\')) return null
        return folder.takeIf { Files.isDirectory(it) }
    }
}
