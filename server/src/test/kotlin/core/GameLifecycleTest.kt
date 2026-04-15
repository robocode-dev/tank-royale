package core

import dev.robocode.tankroyale.schema.BotAddress
import dev.robocode.tankroyale.schema.BotHandshake
import dev.robocode.tankroyale.schema.GameSetup
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.core.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.java_websocket.WebSocket

class GameLifecycleTest : FunSpec({

    tags(Tag("TR-SRV-LIF-001"))

    val config = ServerConfig(
        port = 7654,
        gameTypes = setOf("classic"),
        controllerSecrets = emptySet(),
        botSecrets = emptySet(),
        initialPositionEnabled = false,
        tps = 30
    )

    fun createGameServer(
        connectionHandler: ConnectionHandler,
        participantRegistry: ParticipantRegistry,
        lifecycleManager: GameLifecycleManager
    ): GameServer {
        return GameServer(
            config = config,
            connectionHandler = connectionHandler,
            participantRegistry = participantRegistry,
            lifecycleManager = lifecycleManager,
            broadcaster = mockk(relaxed = true),
            resultsBuilder = mockk(relaxed = true)
        )
    }

    fun createValidGameSetup(): GameSetup {
        return GameSetup().apply {
            gameType = "classic"
            arenaWidth = 800
            arenaHeight = 600
            minNumberOfParticipants = 2
            maxNumberOfParticipants = 10
            numberOfRounds = 3
            gunCoolingRate = 0.1
            maxInactivityTurns = 450
            turnTimeout = 30000
            readyTimeout = 1000000
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
    }

    fun createBotHandshake(name: String): BotHandshake {
        return BotHandshake().also {
            it.name = name
            it.version = "1.0"
            it.gameTypes = listOf("classic")
        }
    }

    test("TR-SRV-LIF-001: Positive: Full game lifecycle transitions") {
        val connectionHandler = mockk<ConnectionHandler>(relaxed = true)
        val participantRegistry = ParticipantRegistry(connectionHandler)
        val lifecycleManager = GameLifecycleManager()
        val gameServer = createGameServer(connectionHandler, participantRegistry, lifecycleManager)

        val bot1 = mockk<WebSocket>()
        val bot2 = mockk<WebSocket>()
        val botAddresses = listOf(mockk<BotAddress>(), mockk<BotAddress>())

        every { connectionHandler.mapToBotSockets(any()) } returns setOf(bot1, bot2)
        every { connectionHandler.getBotHandshakes() } returns mapOf(
            bot1 to createBotHandshake("Bot1"),
            bot2 to createBotHandshake("Bot2")
        )

        // Initial state
        lifecycleManager.serverState shouldBe ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN

        // Start game -> WAIT_FOR_READY_PARTICIPANTS
        val gameSetup = createValidGameSetup()
        gameServer.handleStartGame(gameSetup, botAddresses)
        lifecycleManager.serverState shouldBe ServerState.WAIT_FOR_READY_PARTICIPANTS

        // One bot ready -> still WAIT_FOR_READY_PARTICIPANTS
        gameServer.handleBotReady(bot1)
        lifecycleManager.serverState shouldBe ServerState.WAIT_FOR_READY_PARTICIPANTS

        // Second bot ready -> GAME_RUNNING
        gameServer.handleBotReady(bot2)
        lifecycleManager.serverState shouldBe ServerState.GAME_RUNNING

        // Pause game -> GAME_PAUSED
        gameServer.handlePauseGame()
        lifecycleManager.serverState shouldBe ServerState.GAME_PAUSED

        // Resume game -> GAME_RUNNING
        gameServer.handleResumeGame()
        lifecycleManager.serverState shouldBe ServerState.GAME_RUNNING

        // Abort game -> GAME_STOPPED
        gameServer.handleAbortGame()
        lifecycleManager.serverState shouldBe ServerState.GAME_STOPPED
    }

    test("TR-SRV-LIF-001: Negative: Ignore ready signals when not in WAIT_FOR_READY_PARTICIPANTS") {
        val connectionHandler = mockk<ConnectionHandler>(relaxed = true)
        val participantRegistry = ParticipantRegistry(connectionHandler)
        val lifecycleManager = GameLifecycleManager()
        val gameServer = createGameServer(connectionHandler, participantRegistry, lifecycleManager)

        val bot1 = mockk<WebSocket>()
        every { connectionHandler.getBotHandshakes() } returns mapOf(bot1 to createBotHandshake("Bot1"))

        // Initially in WAIT_FOR_PARTICIPANTS_TO_JOIN
        gameServer.handleBotReady(bot1)
        lifecycleManager.serverState shouldBe ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN
        participantRegistry.readyParticipants shouldBe emptySet()
    }

    test("TR-SRV-LIF-001: Negative: handleStartGame with no valid bots stays in WAIT_FOR_PARTICIPANTS_TO_JOIN") {
        val connectionHandler = mockk<ConnectionHandler>(relaxed = true)
        val participantRegistry = ParticipantRegistry(connectionHandler)
        val lifecycleManager = GameLifecycleManager()
        val gameServer = createGameServer(connectionHandler, participantRegistry, lifecycleManager)

        every { connectionHandler.mapToBotSockets(any()) } returns emptySet()

        val gameSetup = createValidGameSetup()
        gameServer.handleStartGame(gameSetup, emptyList())

        lifecycleManager.serverState shouldBe ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN
    }
})
