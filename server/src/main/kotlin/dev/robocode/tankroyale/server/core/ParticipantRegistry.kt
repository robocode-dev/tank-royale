package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.model.BotId
import org.java_websocket.WebSocket
import java.util.concurrent.ConcurrentHashMap

/** Registry for tracking game participants (bots). */
class ParticipantRegistry(private val connectionHandler: ConnectionHandler) {

    /** Game participants (bots connections) */
    val participants = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Game participants that signalled 'ready' for battle */
    val readyParticipants = ConcurrentHashMap.newKeySet<WebSocket>()

    /** Map over participant ids: bot connection -> bot id */
    val participantIds = ConcurrentHashMap<WebSocket, BotId>()

    /** Map over participants sent to clients */
    val participantMap = ConcurrentHashMap<BotId, Participant>()

    /** Lock for participant-related operations */
    val participantsLock = Any()

    /** Map over debug graphics enable flags */
    val debugGraphicsEnableMap = ConcurrentHashMap<BotId, Boolean /* isDebugEnabled */>()

    fun clear() {
        participantIds.clear()
        readyParticipants.clear()
        participantMap.clear()
        debugGraphicsEnableMap.clear()
    }

    fun prepareParticipantIds() {
        participants.forEachIndexed { index, conn ->
            participantIds[conn] = BotId(index + 1)
        }
    }

    fun createParticipantMap(): Map<BotId, Participant> {
        val map = mutableMapOf<BotId, Participant>()
        for (conn in participants) {
            val handshake = connectionHandler.getBotHandshakes()[conn]
            val botId = participantIds[conn] ?: continue
            val participant = Participant().apply {
                id = botId.value
                sessionId = handshake!!.sessionId
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
                isDroid = handshake.isDroid
            }
            map[botId] = participant
        }
        return map
    }
}
