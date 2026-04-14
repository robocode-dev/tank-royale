package core

import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.core.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.Tag
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class GameServerTpsChangedTest : FunSpec({

    tags(Tag("Legacy"))

    val config = ServerConfig(
        port = 7654,
        gameTypes = setOf("classic"),
        controllerSecrets = emptySet(),
        botSecrets = emptySet(),
        initialPositionEnabled = false,
        tps = 30
    )

    fun createGameServer(broadcaster: MessageBroadcaster): GameServer {
        return GameServer(
            config = config,
            connectionHandler = mockk(),
            participantRegistry = mockk(),
            lifecycleManager = mockk(relaxed = true),
            broadcaster = broadcaster,
            resultsBuilder = mockk(),
            gson = mockk()
        )
    }

    test("handleChangeTps broadcasts TpsChangedEvent with the correct tps value") {
        val mockBroadcaster = mockk<MessageBroadcaster>(relaxed = true)
        val gameServer = createGameServer(mockBroadcaster)

        gameServer.handleChangeTps(60)

        val slot = slot<Message>()
        verify { mockBroadcaster.broadcastToObserverAndControllers(capture(slot)) }

        val event = slot.captured as TpsChangedEvent
        event.tps shouldBe 60
    }

    test("handleChangeTps does not broadcast when tps is unchanged") {
        val mockBroadcaster = mockk<MessageBroadcaster>(relaxed = true)
        val gameServer = createGameServer(mockBroadcaster) // config.tps = 30

        gameServer.handleChangeTps(30) // same as current tps

        verify(exactly = 0) { mockBroadcaster.broadcastToObserverAndControllers(any()) }
    }

    test("handleChangeTps broadcasts different tps values correctly") {
        val mockBroadcaster = mockk<MessageBroadcaster>(relaxed = true)
        val gameServer = createGameServer(mockBroadcaster)

        gameServer.handleChangeTps(10)

        val slot = slot<Message>()
        verify { mockBroadcaster.broadcastToObserverAndControllers(capture(slot)) }
        (slot.captured as TpsChangedEvent).tps shouldBe 10
    }
})
