package core

import dev.robocode.tankroyale.schema.BotAddress
import dev.robocode.tankroyale.schema.BotHandshake
import dev.robocode.tankroyale.schema.GameSetup
import dev.robocode.tankroyale.schema.GameStartedEventForBot
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.core.GameLifecycleManager
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.core.MessageBroadcaster
import dev.robocode.tankroyale.server.core.ParticipantRegistry
import dev.robocode.tankroyale.server.core.ResultsBuilder
import dev.robocode.tankroyale.server.core.ServerConfig
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.java_websocket.WebSocket

class BotNameMappingTest : FunSpec({
    tags(Tag("PRO-010"))
    tags(Tag("Integration"))
    tags(Tag("Positive"))

    test("testPRO010_IntegrationPositive_assignsNamesBySelectedRosterOrder") {
        val events = startBattleAndCaptureEvents()
        val botA = events.getValue("session-a")
        val botB = events.getValue("session-b")
        val botC = events.getValue("session-c")
        val botD = events.getValue("session-d")
        val longName = "com.example.legacy.TeamRobotWithANameLongerThanThirtyCharacters"

        botA.botNames.associate { it.botId to it.name } shouldBe mapOf(
            1 to "$longName 1.2 (2)",
            3 to "Display C 2.0",
            4 to "Display D 3.0"
        )
        botB.botNames.associate { it.botId to it.name } shouldBe mapOf(
            2 to "$longName 1.2 (1)"
        )
        botC.botNames.associate { it.botId to it.name } shouldBe mapOf(
            1 to "$longName 1.2 (2)",
            3 to "Display C 2.0",
            4 to "Display D 3.0"
        )
        botD.botNames.associate { it.botId to it.name } shouldBe mapOf(
            1 to "$longName 1.2 (2)",
            3 to "Display C 2.0",
            4 to "Display D 3.0"
        )
    }
})

internal fun startBattleAndCaptureEvents(): Map<String, GameStartedEventForBot> {
    val connectionHandler = mockk<ConnectionHandler>(relaxed = true)
    val broadcaster = mockk<MessageBroadcaster>(relaxed = true)
    val participantRegistry = ParticipantRegistry(connectionHandler)
    val lifecycleManager = GameLifecycleManager()
    val botA = mockk<WebSocket>()
    val botB = mockk<WebSocket>()
    val botC = mockk<WebSocket>()
    val botD = mockk<WebSocket>()
    val selectedOrder = listOf(botB, botC, botA, botD)
    val handshakes = mapOf(
        botA to handshake("session-a", "Display A", 7, "com.example.legacy.TeamRobotWithANameLongerThanThirtyCharacters"),
        botB to handshake("session-b", "Display B", 9, "com.example.legacy.TeamRobotWithANameLongerThanThirtyCharacters"),
        botC to handshake("session-c", "Display C", 7, ""),
        botD to handshake("session-d", "Display D", 7, null)
    )
    every { connectionHandler.mapToBotSockets(any()) } returns selectedOrder
    every { connectionHandler.getBotHandshakes() } returns handshakes

    val gameSetup = GameSetup().apply {
        gameType = "classic"
        arenaWidth = 800
        arenaHeight = 600
        minNumberOfParticipants = 2
        maxNumberOfParticipants = 10
        numberOfRounds = 1
        gunCoolingRate = 0.1
        maxInactivityTurns = 450
        turnTimeout = 30_000
        readyTimeout = 1_000_000
        defaultTurnsPerSecond = 30
        isArenaWidthLocked = false
        isArenaHeightLocked = false
        isMinNumberOfParticipantsLocked = false
        isMaxNumberOfParticipantsLocked = false
        isNumberOfRoundsLocked = false
        isGunCoolingRateLocked = false
        isMaxInactivityTurnsLocked = false
        isTurnTimeoutLocked = false
        isReadyTimeoutLocked = false
    }
    val server = GameServer(
        config = ServerConfig(
            port = 7654,
            gameTypes = setOf("classic"),
            controllerSecrets = emptySet(),
            botSecrets = emptySet(),
            initialPositionEnabled = false,
            tps = 30
        ),
        connectionHandler = connectionHandler,
        participantRegistry = participantRegistry,
        lifecycleManager = lifecycleManager,
        broadcaster = broadcaster,
        resultsBuilder = mockk<ResultsBuilder>(relaxed = true)
    )

    val capturedEvents = mutableMapOf<WebSocket, GameStartedEventForBot>()
    every { broadcaster.send(any(), any()) } answers {
        capturedEvents[firstArg()] = secondArg()
    }
    server.handleStartGame(gameSetup, List(4) { BotAddress() })
    val eventsBySession = participantRegistry.participantIds.keys.associate { socket ->
        handshakes.getValue(socket).sessionId to capturedEvents.getValue(socket)
    }
    lifecycleManager.stopTimers()
    return eventsBySession
}

internal fun handshake(sessionId: String, name: String, teamId: Int, teamMemberName: String?) =
    BotHandshake().apply {
        this.sessionId = sessionId
        this.name = name
        version = when (sessionId) {
            "session-c" -> "2.0"
            "session-d" -> "3.0"
            else -> "1.2"
        }
        authors = listOf("Test")
        gameTypes = listOf("classic")
        this.teamId = teamId
        this.teamMemberName = teamMemberName
    }
