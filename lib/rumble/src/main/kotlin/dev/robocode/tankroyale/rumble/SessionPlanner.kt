package dev.robocode.tankroyale.rumble

import kotlin.random.Random

/** Which entries a contribution session spends the computer on. */
enum class SelectionMode {
    /** Every battle may use any eligible entry; the selection is ignored. */
    ALL,

    /** A battle with a selected entry is chosen first; only when none is valid does the session use other entries. */
    PREFER,

    /** Every battle contains a selected entry; when none is valid the session waits and never uses other entries. */
    ONLY,
}

/** Why a session has no battle to run right now. The screen and the command line turn the code and its values into text. */
enum class WaitReason {
    /** No game format is chosen. */
    NO_FORMAT,

    /** The mode is [SelectionMode.ONLY] and nothing is selected. */
    NOTHING_SELECTED,

    /** The mode is [SelectionMode.ONLY] and the selected entries are not active entries of the Rumble; the values name them. */
    SELECTED_NOT_ACTIVE,

    /** The mode is [SelectionMode.ONLY] and no valid battle in a chosen format holds a selected entry; the values name the formats. */
    NO_VALID_BATTLE_WITH_SELECTED,

    /** Not even the whole catalog can form a valid battle in a chosen format; the values name the formats. */
    NOT_ENOUGH_ENTRIES,
}

/** What the next step of a session is. */
sealed interface SessionStep {
    /** Run this battle: [participants] are entry keys, `name version`, of individual bots or, for TwinDuel, of two teams. */
    data class Battle(val gameType: String, val participants: List<String>) : SessionStep

    /** There is no battle to run now; the catalog or the selection may change, so ask again later. */
    data class Waiting(val reason: WaitReason, val values: List<String> = emptyList()) : SessionStep

    /** The session has run the battles it was asked to. */
    data object Finished : SessionStep
}

/** What a session has run so far; it is the only state the planner reads, and [after] returns the next one. */
data class SessionHistory(
    val battlesRun: Int = 0,
    val perFormat: Map<String, Int> = emptyMap(),
    val perEntry: Map<String, Int> = emptyMap(),
    /** How often two entries met, by their keys in sorted order joined with a tab. */
    val perPairing: Map<String, Int> = emptyMap(),
) {
    /** The history after [battle] has run to the end. */
    fun after(battle: SessionStep.Battle): SessionHistory {
        val keys = battle.participants
        val pairings = perPairing.toMutableMap()
        for (a in keys) for (b in keys) if (a < b) pairings.merge(pairingKey(a, b), 1, Int::plus)
        val entries = perEntry.toMutableMap()
        keys.forEach { entries.merge(it, 1, Int::plus) }
        return SessionHistory(battlesRun + 1, perFormat + (battle.gameType to (perFormat[battle.gameType] ?: 0) + 1), entries, pairings)
    }

    internal fun met(a: String, b: String): Int = perPairing[pairingKey(minOf(a, b), maxOf(a, b))] ?: 0

    internal companion object {
        fun pairingKey(a: String, b: String): String = "$a\t$b"
    }
}

/** What a session was asked to do. */
data class SessionRequest(
    /** The entries of the catalog; only active ones are used. */
    val entries: List<CatalogBot>,
    /** Keys, `name version`, of the entries the contributor chose; it may name entries that are no longer active. */
    val selected: Set<String>,
    val mode: SelectionMode,
    /** The game formats chosen, in the order the contributor listed them. */
    val gameTypes: List<String>,
    /** How many bots a battle holds in each game format the Rumble runs, for TwinDuel the bots of both teams together. */
    val participantCounts: Map<String, Int>,
    /** The number of battles to run, or null to go on until stopped. */
    val battleLimit: Int? = null,
    /** The published advice per game type, best first, each a group of entry keys that should meet. A format may have none. */
    val advice: Map<String, List<List<String>>> = emptyMap(),
    /** Makes the choice among equals repeatable. */
    val seed: Long = 0,
)

/**
 * Chooses the next ranked battle of a contribution session. It reads only its arguments, so the desktop program and the
 * command line, which call it alike, cannot disagree about it, and a session is repeatable from its seed.
 */
object SessionPlanner {

    /** How many of the best advised groups are drawn from, so that clients reading the same advice spread out. */
    private const val ADVICE_BREADTH = 5

    fun key(bot: CatalogBot): String = "${bot.name} ${bot.version}"

    fun next(request: SessionRequest, history: SessionHistory): SessionStep {
        val limit = request.battleLimit
        if (limit != null && history.battlesRun >= limit) return SessionStep.Finished
        if (request.gameTypes.isEmpty()) return SessionStep.Waiting(WaitReason.NO_FORMAT)

        val pool = Pool(request.entries.filter { it.isActive })
        val formats = request.gameTypes.distinct().sortedBy { history.perFormat[it] ?: 0 } // stable: ties keep the contributor's order
        val random = Random(request.seed * 31 + history.battlesRun)

        val selected = request.selected.filter { it in pool.byKey }.toSet()
        val useSelected = request.mode != SelectionMode.ALL && selected.isNotEmpty()

        if (request.mode == SelectionMode.ONLY) {
            if (request.selected.isEmpty()) return SessionStep.Waiting(WaitReason.NOTHING_SELECTED)
            if (selected.isEmpty()) return SessionStep.Waiting(WaitReason.SELECTED_NOT_ACTIVE, request.selected.sorted())
        }

        if (useSelected) {
            for (format in formats) {
                val battle = plan(request, history, pool, format, selected, random)
                if (battle != null) return battle
            }
            if (request.mode == SelectionMode.ONLY) {
                val anywhere = formats.filter { plan(request, history, pool, it, null, random) != null }
                return if (anywhere.isEmpty()) SessionStep.Waiting(WaitReason.NOT_ENOUGH_ENTRIES, formats.sorted())
                else SessionStep.Waiting(WaitReason.NO_VALID_BATTLE_WITH_SELECTED, formats.sorted())
            }
        }

        for (format in formats) {
            val battle = plan(request, history, pool, format, null, random)
            if (battle != null) return battle
        }
        return SessionStep.Waiting(WaitReason.NOT_ENOUGH_ENTRIES, formats.sorted())
    }

    /** The entries a battle may use, by their keys. */
    private class Pool(val bots: List<CatalogBot>) {
        val byKey: Map<String, CatalogBot> = bots.associateBy { key(it) }
        val individuals: List<String> = bots.filter { it.teamMembers.isEmpty() }.map { key(it) }.sorted()
        val teams: List<String> = bots.filter { it.teamMembers.isNotEmpty() }.map { key(it) }.sorted()

        /** The members of [team] when each is an active individual, otherwise null. */
        fun members(team: String): List<String>? {
            val members = byKey[team]?.teamMembers ?: return null
            return if (members.all { m -> m in byKey && byKey.getValue(m).teamMembers.isEmpty() }) members else null
        }
    }

    /**
     * The best valid battle of [format], or null when there is none. With [mustHold] set, the battle holds at least one of
     * those entries, and the one used least so far takes its turn first.
     */
    private fun plan(
        request: SessionRequest,
        history: SessionHistory,
        pool: Pool,
        format: String,
        mustHold: Set<String>?,
        random: Random,
    ): SessionStep.Battle? {
        val count = request.participantCounts[format] ?: return null
        val twinDuel = format == "twinduel"
        val candidates = if (twinDuel) pool.teams.filter { pool.members(it)?.size == count / 2 } else pool.individuals
        val perBattle = if (twinDuel) 2 else count
        if (candidates.size < perBattle || perBattle < 2) return null

        val advice = request.advice[format].orEmpty().map { group -> group.filter { it in candidates }.distinct() }

        val anchors = if (mustHold == null) listOf(null) else candidates.filter { it in mustHold }.shuffled(random).sortedBy { history.perEntry[it] ?: 0 }
        for (anchor in anchors) {
            val chosen = group(anchor, candidates, perBattle, advice, history, random, twinDuel, pool, count) ?: continue
            return SessionStep.Battle(format, chosen)
        }
        return null
    }

    private fun group(
        anchor: String?,
        candidates: List<String>,
        perBattle: Int,
        advice: List<List<String>>,
        history: SessionHistory,
        random: Random,
        twinDuel: Boolean,
        pool: Pool,
        count: Int,
    ): List<String>? {
        fun valid(group: List<String>): Boolean {
            if (group.size != perBattle || group.distinct().size != perBattle) return false
            if (!twinDuel) return true
            val all = group.flatMap { pool.members(it).orEmpty() }
            return all.size == count && all.distinct().size == count
        }

        // Advice for the anchor, or for anyone when there is no anchor, as a group that already holds enough entries.
        val advised = advice.filter { anchor == null || anchor in it }
        val whole = advised.filter { valid(it) }
        if (whole.isNotEmpty()) return whole.take(ADVICE_BREADTH).random(random)

        // A short advised group is filled up; its members come first.
        val seed = advised.firstOrNull()?.let { g -> g.take(perBattle) } ?: emptyList()
        val start = (listOfNotNull(anchor) + seed).distinct().take(perBattle)

        fun cost(chosen: List<String>, c: String) =
            chosen.sumOf { history.met(minOf(c, it), maxOf(c, it)) } * 1000 + (history.perEntry[c] ?: 0)

        // The least used entry that still fits comes first, but another is tried when it leaves no way to fill the battle:
        // two TwinDuel teams may each fit the first, yet clash with every other team.
        fun fill(chosen: List<String>, rest: List<String>): List<String>? {
            if (chosen.size == perBattle) return chosen.takeIf { valid(it) }
            val fits = rest.filter { it !in chosen && compatible(chosen + it, twinDuel, pool) }.sortedBy { cost(chosen, it) }
            for (c in fits) fill(chosen + c, rest)?.let { return it }
            return null
        }
        if (!compatible(start, twinDuel, pool)) return null
        return fill(start, candidates.shuffled(random))
    }

    /** True when the teams so far share no member bot, which is the only way a TwinDuel group can stay valid. */
    private fun compatible(group: List<String>, twinDuel: Boolean, pool: Pool): Boolean {
        if (!twinDuel) return true
        val members = group.flatMap { pool.members(it).orEmpty() }
        return members.size == members.distinct().size
    }
}
