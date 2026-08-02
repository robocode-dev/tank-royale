package connection

import com.google.gson.Gson
import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.schema.ServerHandshake
import dev.robocode.tankroyale.server.connection.ClientWebSocketsHandler
import dev.robocode.tankroyale.server.connection.IConnectionListener
import dev.robocode.tankroyale.server.core.ServerSetup
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

class ServerHandshakeTest : FunSpec({

    tags(Tag("PRO-005"))

    test("PRO-005: Positive: server handshake advertises behavior version") {
        val socket = mockk<WebSocket>(relaxed = true)
        var sentMessage: String? = null
        every { socket.send(any<String>()) } answers { sentMessage = firstArg<String>() }

        val handler = ClientWebSocketsHandler(
            setup = ServerSetup(setOf("classic", "twinduel")),
            listener = mockk<IConnectionListener>(relaxed = true),
            controllerSecrets = emptySet(),
            botSecrets = emptySet(),
            debugModeSupported = false,
            breakpointModeSupported = false,
            broadcastFunction = { _, _ -> },
        )

        try {
            handler.onOpen(socket, mockk<ClientHandshake>(relaxed = true))
            verify(timeout = 1_000, exactly = 1) { socket.send(any<String>()) }

            val handshake = Gson().fromJson(requireNotNull(sentMessage), ServerHandshake::class.java)
            handshake.behaviorVersion shouldBe CURRENT_BEHAVIOR_VERSION
            handshake.gameTypes shouldBe setOf("classic", "twinduel")
        } finally {
            handler.close()
        }
    }
})
