package connection

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.robocode.tankroyale.server.connection.ClientWebSocketsHandler
import dev.robocode.tankroyale.server.connection.IConnectionListener
import dev.robocode.tankroyale.server.core.ServerSetup
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake

class TeamMessageRawClientTest : FunSpec({
    val listener = mockk<IConnectionListener>(relaxed = true)
    val handler = ClientWebSocketsHandler(
        setup = ServerSetup(),
        listener = listener,
        controllerSecrets = emptySet(),
        botSecrets = emptySet(),
        debugModeSupported = false,
        breakpointModeSupported = false,
        broadcastFunction = { _, _ -> },
    )

    afterSpec { handler.close() }

    test("testPRO007_UnitNegative_rejectsMalformedBatchWithoutForwardingIntent")
        .config(tags = setOf(Tag("PRO-007"))) {
        val socket = mockk<WebSocket>(relaxed = true)
        var handshakeJson: String? = null
        every { socket.send(any<String>()) } answers { handshakeJson = firstArg() }

        handler.onOpen(socket, mockk<ClientHandshake>(relaxed = true))
        verify(timeout = 1_000) { socket.send(any<String>()) }
        val sessionId = Gson().fromJson(requireNotNull(handshakeJson), JsonObject::class.java)
            .get("sessionId").asString
        val botHandshake = JsonObject().apply {
            addProperty("type", "BotHandshake")
            addProperty("sessionId", sessionId)
            addProperty("name", "RawBatchClient")
            addProperty("version", "1.4.0")
            add("authors", Gson().toJsonTree(listOf("test")))
        }
        handler.onMessage(socket, botHandshake.toString())
        verify(timeout = 1_000) { listener.onBotJoined(socket, any()) }

        val rawIntent = """
            {"type":"BotIntent","teamMessages":[
              {"message":"\"valid\"","messageType":"java.lang.String"},
              {"message":"{broken","messageType":"team-message-batch-v1"}
            ]}
        """.trimIndent()
        handler.onMessage(socket, rawIntent)

        verify(timeout = 1_000) {
            socket.close(1008, match { it.contains("Invalid team message batch payload") })
        }
        verify(exactly = 0) { listener.onBotIntent(any(), any(), any()) }
    }

    test("testPRO008_UnitNegative_rejectsWebSocketTextLargerThanOneMiBBeforeParsing")
        .config(tags = setOf(Tag("PRO-008"))) {
        val socket = mockk<WebSocket>(relaxed = true)
        val oversized = "{" + "x".repeat(1_048_576)

        handler.onMessage(socket, oversized)

        verify(exactly = 1) {
            socket.close(1009, match { it.contains("1048576 UTF-8 bytes") })
        }
    }
})
