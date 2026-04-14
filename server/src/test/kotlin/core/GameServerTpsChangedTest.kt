package core

import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.schema.TpsChangedEvent
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.core.MessageBroadcaster
import dev.robocode.tankroyale.server.core.ServerConfig
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

    fun GameServer.replaceBroadcaster(mock: MessageBroadcaster) {
        GameServer::class.java.getDeclaredField("broadcaster").apply {
            isAccessible = true
        }.set(this, mock)
    }

    fun GameServer.callHandleChangeTps(newTps: Int) {
        GameServer::class.java.declaredMethods
            .first { it.name.startsWith("handleChangeTps") }
            .apply { isAccessible = true }
            .invoke(this, newTps)
    }

    test("handleChangeTps broadcasts TpsChangedEvent with the correct tps value") {
        val gameServer = GameServer(config)
        val mockBroadcaster = mockk<MessageBroadcaster>(relaxed = true)
        gameServer.replaceBroadcaster(mockBroadcaster)

        gameServer.callHandleChangeTps(60)

        val slot = slot<Message>()
        verify { mockBroadcaster.broadcastToObserverAndControllers(capture(slot)) }

        val event = slot.captured as TpsChangedEvent
        event.tps shouldBe 60
    }

    test("handleChangeTps does not broadcast when tps is unchanged") {
        val gameServer = GameServer(config) // config.tps = 30
        val mockBroadcaster = mockk<MessageBroadcaster>(relaxed = true)
        gameServer.replaceBroadcaster(mockBroadcaster)

        gameServer.callHandleChangeTps(30) // same as current tps

        verify(exactly = 0) { mockBroadcaster.broadcastToObserverAndControllers(any()) }
    }

    test("handleChangeTps broadcasts different tps values correctly") {
        val gameServer = GameServer(config)
        val mockBroadcaster = mockk<MessageBroadcaster>(relaxed = true)
        gameServer.replaceBroadcaster(mockBroadcaster)

        gameServer.callHandleChangeTps(10)

        val slot = slot<Message>()
        verify { mockBroadcaster.broadcastToObserverAndControllers(capture(slot)) }
        (slot.captured as TpsChangedEvent).tps shouldBe 10
    }
})
