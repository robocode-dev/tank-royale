package dev.robocode.tankroyale.intent

import kotlinx.serialization.json.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Transparent WebSocket proxy that forwards all messages between bots and the server
 * while capturing `BotIntent` messages for diagnostics.
 *
 * Each bot connection to the proxy gets a corresponding relay connection to the real server.
 * All messages are forwarded as-is; the proxy only inspects messages it needs to capture
 * (`BotIntent`, `BotHandshake`, `TickEventForBot`).
 *
 * This class is designed as a reusable library component (per Design Decision 8) that
 * can be integrated by the Battle Runner or the GUI independently.
 *
 * @param serverUrl the real server's WebSocket URL (e.g. `ws://localhost:7654`)
 */
class IntentDiagnosticsProxy(
    private val serverUrl: String,
) : AutoCloseable {

    private val logger = Logger.getLogger(IntentDiagnosticsProxy::class.java.name)
    private val json = Json { ignoreUnknownKeys = true }

    private var proxyServer: ProxyWebSocketServer? = null
    private val relays = ConcurrentHashMap<WebSocket, RelayConnection>()

    /** Captured bot intents. Query this after a battle to inspect intent data. */
    val store = IntentStore()

    /** The port the proxy is listening on. Only valid after [start]. */
    var port: Int = 0
        private set

    /** The WebSocket URL bots should connect to. Only valid after [start]. */
    val proxyUrl: String
        get() = "ws://localhost:$port"

    /** True if the proxy server is currently running. */
    val isRunning: Boolean
        get() = proxyServer != null

    // -------------------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------------------

    /**
     * Starts the proxy server on a dynamically assigned port.
     *
     * @throws IllegalStateException if the proxy fails to start
     */
    fun start() {
        if (proxyServer != null) return

        val freePort = ServerSocket(0).use { it.localPort }
        val startLatch = CountDownLatch(1)

        val server = ProxyWebSocketServer(InetSocketAddress(freePort), startLatch)
        server.isReuseAddr = true
        server.start()

        if (!startLatch.await(START_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            server.stop()
            throw IllegalStateException(
                "Intent diagnostics proxy failed to start within ${START_TIMEOUT_MS}ms"
            )
        }

        port = freePort
        proxyServer = server
    }

    /** Stops the proxy server and closes all relay connections. */
    override fun close() {
        relays.values.forEach { it.close() }
        relays.clear()

        proxyServer?.let { server ->
            try {
                server.stop(STOP_TIMEOUT_MS)
            } catch (e: Exception) {
                logger.log(Level.FINE, "Error stopping proxy server", e)
            }
        }
        proxyServer = null
    }

    // -------------------------------------------------------------------------------------
    // Per-connection relay
    // -------------------------------------------------------------------------------------

    private inner class RelayConnection(val botSocket: WebSocket) {

        var serverWs: java.net.http.WebSocket? = null
        var botName: String = ""
        var botVersion: String = ""
        var lastRoundNumber: Int = 0
        var lastTurnNumber: Int = 0

        private val payload = AtomicReference(StringBuffer())

        fun connectToServer() {
            val httpClient = HttpClient.newBuilder().build()

            val listener = object : java.net.http.WebSocket.Listener {
                override fun onText(
                    webSocket: java.net.http.WebSocket,
                    data: CharSequence?,
                    last: Boolean,
                ): CompletionStage<*>? {
                    val buf = payload.get()
                    buf.append(data)
                    if (last) {
                        val message = buf.toString()
                        buf.delete(0, buf.length)
                        handleServerMessage(message)
                    }
                    webSocket.request(1)
                    return null
                }

                override fun onClose(
                    webSocket: java.net.http.WebSocket?,
                    statusCode: Int,
                    reason: String?,
                ): CompletionStage<*>? {
                    closeBotSocket()
                    return null
                }

                override fun onError(webSocket: java.net.http.WebSocket?, error: Throwable?) {
                    logger.log(Level.FINE, "Relay server connection error for bot '$botName'", error)
                    closeBotSocket()
                }
            }

            try {
                serverWs = httpClient.newWebSocketBuilder()
                    .buildAsync(URI(serverUrl), listener)
                    .join()
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Failed to connect relay to server at $serverUrl", e)
                closeBotSocket()
            }
        }

        /** Forward a message from the server to the bot. Track turn numbers. */
        fun handleServerMessage(message: String) {
            trackTurnNumber(message)

            try {
                if (botSocket.isOpen) {
                    botSocket.send(message)
                }
            } catch (e: Exception) {
                logger.log(Level.FINE, "Failed to forward server→bot message for '$botName'", e)
            }
        }

        /** Forward a message from the bot to the server. Capture intents. */
        fun handleBotMessage(message: String) {
            captureBotMessage(message)

            try {
                serverWs?.sendText(message, true)
            } catch (e: Exception) {
                logger.log(Level.FINE, "Failed to forward bot→server message for '$botName'", e)
            }
        }

        private fun captureBotMessage(message: String) {
            try {
                val jsonObj = json.parseToJsonElement(message).jsonObject
                val type = jsonObj["type"]?.jsonPrimitive?.content ?: return

                when (type) {
                    TYPE_BOT_HANDSHAKE -> {
                        botName = jsonObj["name"]?.jsonPrimitive?.content ?: "unknown"
                        botVersion = jsonObj["version"]?.jsonPrimitive?.content ?: ""
                    }

                    TYPE_BOT_INTENT -> {
                        val intent = json.decodeFromJsonElement<BotIntent>(jsonObj)
                        store.add(
                            CapturedIntent(
                                botName = botName,
                                botVersion = botVersion,
                                roundNumber = lastRoundNumber,
                                turnNumber = lastTurnNumber,
                                intent = intent,
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                logger.log(Level.FINE, "Failed to parse bot message for capture", e)
            }
        }

        private fun trackTurnNumber(message: String) {
            try {
                val jsonObj = json.parseToJsonElement(message).jsonObject
                val type = jsonObj["type"]?.jsonPrimitive?.content ?: return

                when (type) {
                    TYPE_TICK_EVENT -> {
                        jsonObj["turnNumber"]?.jsonPrimitive?.int?.let { lastTurnNumber = it }
                        jsonObj["roundNumber"]?.jsonPrimitive?.int?.let { lastRoundNumber = it }
                    }

                    TYPE_ROUND_STARTED -> {
                        jsonObj["roundNumber"]?.jsonPrimitive?.int?.let { lastRoundNumber = it }
                        lastTurnNumber = 0
                    }
                }
            } catch (_: Exception) {
                // Not a message we need to track
            }
        }

        private fun closeBotSocket() {
            try {
                if (botSocket.isOpen) botSocket.close()
            } catch (_: Exception) {
            }
        }

        fun close() {
            try {
                serverWs?.abort()
            } catch (_: Exception) {
            }
            closeBotSocket()
        }
    }

    // -------------------------------------------------------------------------------------
    // WebSocket server for accepting bot connections
    // -------------------------------------------------------------------------------------

    private inner class ProxyWebSocketServer(
        address: InetSocketAddress,
        private val startLatch: CountDownLatch,
    ) : WebSocketServer(address) {

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            val relay = RelayConnection(conn)
            relays[conn] = relay
            relay.connectToServer()
        }

        override fun onMessage(conn: WebSocket, message: String) {
            relays[conn]?.handleBotMessage(message)
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
            relays.remove(conn)?.close()
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            if (conn != null) {
                logger.log(Level.FINE, "Proxy connection error", ex)
                relays.remove(conn)?.close()
            } else {
                logger.log(Level.WARNING, "Proxy server error", ex)
            }
        }

        override fun onStart() {
            startLatch.countDown()
        }
    }

    companion object {
        private const val START_TIMEOUT_MS = 5000L
        private const val STOP_TIMEOUT_MS = 1000

        // Message type discriminator values (from message.schema.yaml)
        private const val TYPE_BOT_HANDSHAKE = "BotHandshake"
        private const val TYPE_BOT_INTENT = "BotIntent"
        private const val TYPE_TICK_EVENT = "TickEventForBot"
        private const val TYPE_ROUND_STARTED = "RoundStartedEvent"
    }
}
