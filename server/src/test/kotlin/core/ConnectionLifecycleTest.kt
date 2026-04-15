package core

import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.core.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.java_websocket.WebSocket

class ConnectionLifecycleTest : FunSpec({

    tags(Tag("TR-SRV-CON-001"))

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
        lifecycleManager: GameLifecycleManager,
        broadcaster: MessageBroadcaster
    ): GameServer {
        return GameServer(
            config = config,
            connectionHandler = connectionHandler,
            participantRegistry = participantRegistry,
            lifecycleManager = lifecycleManager,
            broadcaster = broadcaster,
            resultsBuilder = mockk(relaxed = true)
        )
    }

    test("TR-SRV-CON-001: Positive: Bot join/leave lifecycle") {
        val connectionHandler = mockk<ConnectionHandler>(relaxed = true)
        val participantRegistry = ParticipantRegistry(connectionHandler)
        val lifecycleManager = GameLifecycleManager()
        val broadcaster = mockk<MessageBroadcaster>(relaxed = true)
        val gameServer = createGameServer(connectionHandler, participantRegistry, lifecycleManager, broadcaster)

        val bot1 = mockk<WebSocket>()
        
        // Bot joins
        participantRegistry.addParticipant(bot1)
        gameServer.handleBotJoined()
        
        participantRegistry.participants shouldBe setOf(bot1)
        verify { broadcaster.broadcastBotListUpdate() }
        
        // Bot leaves (while IDLE)
        gameServer.handleBotLeft(bot1)
        participantRegistry.participants shouldBe emptySet()
        verify(exactly = 2) { broadcaster.broadcastBotListUpdate() } // once for join, once for left
    }

    test("TR-SRV-CON-001: Positive: Abort game when last bot leaves") {
        val connectionHandler = mockk<ConnectionHandler>(relaxed = true)
        val participantRegistry = ParticipantRegistry(connectionHandler)
        val lifecycleManager = GameLifecycleManager()
        val broadcaster = mockk<MessageBroadcaster>(relaxed = true)
        val gameServer = createGameServer(connectionHandler, participantRegistry, lifecycleManager, broadcaster)

        val bot1 = mockk<WebSocket>()
        participantRegistry.addParticipant(bot1)
        
        // Start game
        lifecycleManager.serverState = ServerState.GAME_RUNNING
        
        // Bot leaves
        gameServer.handleBotLeft(bot1)
        
        lifecycleManager.serverState shouldBe ServerState.GAME_STOPPED
        verify { broadcaster.broadcastToObserverAndControllers(any()) } // GameAbortedEvent
    }
})
