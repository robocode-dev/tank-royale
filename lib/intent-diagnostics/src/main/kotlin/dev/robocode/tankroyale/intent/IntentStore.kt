package dev.robocode.tankroyale.intent

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread-safe in-memory storage for captured bot intents.
 *
 * Stores [CapturedIntent] instances per bot name. Query methods return
 * immutable snapshots — callers can read while the proxy continues writing.
 */
class IntentStore {

    private val intentsByBot = ConcurrentHashMap<String, CopyOnWriteArrayList<CapturedIntent>>()

    /** Records a captured intent. */
    fun add(intent: CapturedIntent) {
        intentsByBot.computeIfAbsent(intent.botName) { CopyOnWriteArrayList() }
            .add(intent)
    }

    /** Returns all captured intents, grouped by bot name. */
    fun getAllIntents(): Map<String, List<CapturedIntent>> =
        intentsByBot.mapValues { it.value.toList() }

    /** Returns captured intents for a specific bot. */
    fun getIntentsForBot(botName: String): List<CapturedIntent> =
        intentsByBot[botName]?.toList() ?: emptyList()

    /** Returns the intent for a specific bot at a specific round and turn, or `null`. */
    fun getIntentForBotAtTurn(botName: String, roundNumber: Int, turnNumber: Int): CapturedIntent? =
        intentsByBot[botName]?.find { it.roundNumber == roundNumber && it.turnNumber == turnNumber }

    /** Returns the set of all bot names that have recorded intents. */
    fun botNames(): Set<String> = intentsByBot.keys.toSet()

    /** Clears all captured intents. Call between battles. */
    fun clear() {
        intentsByBot.clear()
    }

    /** Total number of captured intents across all bots. */
    val size: Int
        get() = intentsByBot.values.sumOf { it.size }
}
