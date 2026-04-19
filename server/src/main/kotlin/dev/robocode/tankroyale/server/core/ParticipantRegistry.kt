package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.model.BotId
import org.java_websocket.WebSocket
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for tracking game participants (bots).
 *
 * Thread-safety contract:
 * - Individual read/write operations on the underlying [ConcurrentHashMap]-backed collections are
 *   thread-safe in isolation.
 * - Compound operations (e.g., check-then-act, iterate-then-remove) must be performed while holding
 *   [participantsLock] to avoid TOCTOU races.
 */
class ParticipantRegistry(private val connectionHandler: ConnectionHandler) {

    private val _participants = ConcurrentHashMap.newKeySet<WebSocket>()
    private val _readyParticipants = ConcurrentHashMap.newKeySet<WebSocket>()
    private val _participantIds = ConcurrentHashMap<WebSocket, BotId>()
    internal val _participantMap = ConcurrentHashMap<BotId, Participant>()
    private val _debugGraphicsEnableMap = ConcurrentHashMap<BotId, Boolean /* isDebugEnabled */>()
    private val _breakpointEnabledMap = ConcurrentHashMap<BotId, Boolean /* isBreakpointEnabled */>()

    /** Read-only view of game participants (bot connections) */
    val participants: Set<WebSocket> get() = _participants

    /** Read-only view of participants that signalled 'ready' for battle */
    val readyParticipants: Set<WebSocket> get() = _readyParticipants

    /** Read-only view of participant ids: bot connection -> bot id */
    val participantIds: Map<WebSocket, BotId> get() = _participantIds

    /** Read-only view of participants sent to clients */
    val participantMap: Map<BotId, Participant> get() = _participantMap

    /** Read-only view of debug graphics enables flags */
    val debugGraphicsEnableMap: Map<BotId, Boolean> get() = _debugGraphicsEnableMap

    /** Lock for participant-related operations */
    val participantsLock = Any()

    fun addParticipant(conn: WebSocket) {
        _participants += conn
    }

    fun removeParticipant(conn: WebSocket): Boolean = _participants.remove(conn)

    fun setParticipants(conns: Collection<WebSocket>) {
        _participants.clear()
        _participants += conns
    }

    fun addReadyParticipant(conn: WebSocket) {
        _readyParticipants += conn
    }

    fun removeNonReadyParticipants(): List<WebSocket> {
        val removed = mutableListOf<WebSocket>()
        val iterator = _participants.iterator()
        while (iterator.hasNext()) {
            val conn = iterator.next()
            if (!_readyParticipants.contains(conn)) {
                iterator.remove()
                _participantIds.remove(conn)
                removed += conn
            }
        }
        return removed
    }

    fun populateParticipantMap() {
        _participantMap.putAll(createParticipantMap())
    }

    fun clearReadyParticipants() {
        _readyParticipants.clear()
    }

    fun setDebugGraphicsEnabled(botId: BotId, enabled: Boolean) {
        _debugGraphicsEnableMap[botId] = enabled
    }

    fun setBreakpointEnabled(botId: BotId, enabled: Boolean) {
        _breakpointEnabledMap[botId] = enabled
    }

    val breakpointEnabledMap: Map<BotId, Boolean> get() = _breakpointEnabledMap

    fun clear() {
        _participantIds.clear()
        _readyParticipants.clear()
        _participantMap.clear()
        _debugGraphicsEnableMap.clear()
        _breakpointEnabledMap.clear()
    }

    fun prepareParticipantIds() {
        _participants.forEachIndexed { index, conn ->
            _participantIds[conn] = BotId(index + 1)
        }
    }

    private fun createParticipantMap(): Map<BotId, Participant> {
        val map = mutableMapOf<BotId, Participant>()
        for (conn in _participants) {
            val handshake = connectionHandler.getBotHandshakes()[conn] ?: continue
            val botId = _participantIds[conn] ?: continue
            val participant = Participant().apply {
                id = botId.value
                sessionId = handshake.sessionId
                name = handshake.name
                version = handshake.version
                description = handshake.description
                authors = handshake.authors
                homepage = handshake.homepage
                countryCodes = handshake.countryCodes
                gameTypes = handshake.gameTypes
                platform = handshake.platform
                programmingLang = handshake.programmingLang
                teamId = handshake.teamId
                teamName = handshake.teamName
                teamVersion = handshake.teamVersion
                isDroid = handshake.isDroid
                debuggerAttached = handshake.debuggerAttached
            }
            map[botId] = participant
        }
        return map
    }
}
