package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.runner.BattleException
import dev.robocode.tankroyale.runner.BattleResults
import dev.robocode.tankroyale.runner.BotResult
import kotlinx.serialization.PolymorphicSerializer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionStage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Manages dual WebSocket connections (Observer + Controller) to the Tank Royale server.
 *
 * Handles the handshake protocol, message dispatching, and exposes typed events for all
 * server messages. Unlike the `lib/client` WebSocketClient which uses global singleton events,
 * this class provides per-instance events to support concurrent connections.
 *
 * This class is internal to the runner module and is not part of the public API.
 */
internal class ServerConnection(
    private val serverUrl: String,
    private val controllerSecret: String,
) : AutoCloseable {

    private val logger = Logger.getLogger(ServerConnection::class.java.name)
    private val json = MessageConstants.json

    private var observerWs: WebSocket? = null
    private var controllerWs: WebSocket? = null
    private val connected = AtomicBoolean(false)

    // -------------------------------------------------------------------------------------
    // Events (6.8)
    // -------------------------------------------------------------------------------------

    val onBotListUpdate = Event<BotListUpdate>()
    val onGameStarted = Event<GameStartedEvent>()
    val onGameEnded = Event<GameEndedEvent>()
    val onGameAborted = Event<GameAbortedEvent>()
    val onGamePaused = Event<GamePausedEvent>()
    val onGameResumed = Event<GameResumedEvent>()
    val onRoundStarted = Event<RoundStartedEvent>()
    val onRoundEnded = Event<RoundEndedEvent>()
    val onTickEvent = Event<TickEvent>()
    val onTpsChanged = Event<TpsChangedEvent>()

    /** Fires the raw JSON string for every message received on the Observer connection. */
    val onRawObserverMessage = Event<String>()

    /** The most recently received set of bots from BotListUpdate. */
    val latestBotList = AtomicReference<Set<BotInfo>>(emptySet())

    /** Server features advertised in the ServerHandshake (available after connect). */
    val serverFeatures = AtomicReference<Features?>(null)

    /** True if both Observer and Controller connections are established. */
    val isConnected: Boolean get() = connected.get()

    // -------------------------------------------------------------------------------------
    // 6.1 / 6.2 — Observer + Controller connections
    // -------------------------------------------------------------------------------------

    /**
     * Opens Observer and Controller WebSocket connections to the server.
     * Performs the full handshake sequence for both roles.
     *
     * @throws BattleException if connections cannot be established within the timeout
     */
    fun connect() {
        if (connected.get()) return

        val observerReady = CountDownLatch(1)
        val controllerReady = CountDownLatch(1)

        // Connect Observer (6.1)
        observerWs = openWebSocket(serverUrl, Role.OBSERVER, observerReady)

        if (!observerReady.await(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            throw BattleException("Observer handshake with server at $serverUrl timed out")
        }

        // Connect Controller (6.2)
        controllerWs = openWebSocket(serverUrl, Role.CONTROLLER, controllerReady)

        if (!controllerReady.await(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            throw BattleException("Controller handshake with server at $serverUrl timed out")
        }

        connected.set(true)
    }

    private enum class Role { OBSERVER, CONTROLLER }

    private fun openWebSocket(url: String, role: Role, readyLatch: CountDownLatch): WebSocket {
        val httpClient = HttpClient.newBuilder().build()
        val payload = AtomicReference(StringBuffer())

        val listener = object : WebSocket.Listener {
            override fun onText(webSocket: WebSocket, data: CharSequence?, last: Boolean): CompletionStage<*>? {
                val buf = payload.get()
                buf.append(data)
                if (last) {
                    val message = buf.toString()
                    buf.delete(0, buf.length)
                    handleMessage(message, role, webSocket, readyLatch)
                }
                webSocket.request(1)
                return null
            }

            override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*>? {
                connected.set(false)
                return null
            }

            override fun onError(webSocket: WebSocket?, error: Throwable) {
                logger.log(Level.WARNING, "WebSocket error ($role): ${error.message}", error)
                connected.set(false)
            }
        }

        return try {
            httpClient.newWebSocketBuilder()
                .buildAsync(URI(url), listener)
                .join()
        } catch (e: Exception) {
            throw BattleException("Failed to connect $role WebSocket to $url", e)
        }
    }

    private fun handleMessage(message: String, role: Role, ws: WebSocket, readyLatch: CountDownLatch) {
        // Fire raw message for recording support (Observer only)
        if (role == Role.OBSERVER) onRawObserverMessage(message)

        try {
            when (val msg = json.decodeFromString(PolymorphicSerializer(Message::class), message)) {
                is ServerHandshake -> {
                    serverFeatures.set(msg.features)
                    sendHandshakeResponse(msg, role, ws)
                    // Handshake complete after we send our response — server will reply with BotListUpdate
                    readyLatch.countDown()
                }

                // Route events only from Observer connection to avoid duplicates
                is BotListUpdate -> if (role == Role.OBSERVER) {
                    latestBotList.set(msg.bots)
                    onBotListUpdate(msg)
                }
                is GameStartedEvent -> if (role == Role.OBSERVER) onGameStarted(msg)
                is GameEndedEvent -> if (role == Role.OBSERVER) onGameEnded(msg)
                is GameAbortedEvent -> if (role == Role.OBSERVER) onGameAborted(msg)
                is GamePausedEvent -> if (role == Role.OBSERVER) onGamePaused(msg)
                is GameResumedEvent -> if (role == Role.OBSERVER) onGameResumed(msg)
                is RoundStartedEvent -> if (role == Role.OBSERVER) onRoundStarted(msg)
                is RoundEndedEvent -> if (role == Role.OBSERVER) onRoundEnded(msg)
                is TickEvent -> if (role == Role.OBSERVER) onTickEvent(msg)
                is TpsChangedEvent -> if (role == Role.OBSERVER) onTpsChanged(msg)
                else -> {} // Ignore unknown messages
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to handle message: ${e.message}", e)
        }
    }

    private fun sendHandshakeResponse(serverHandshake: ServerHandshake, role: Role, ws: WebSocket) {
        val handshake: Message = when (role) {
            Role.OBSERVER -> ObserverHandshake(
                sessionId = serverHandshake.sessionId,
                name = "Robocode Tank Royale Battle Runner",
                version = Version.version,
                author = null,
                secret = controllerSecret,
            )
            Role.CONTROLLER -> ControllerHandshake(
                sessionId = serverHandshake.sessionId,
                name = "Robocode Tank Royale Battle Runner",
                version = Version.version,
                author = null,
                secret = controllerSecret,
            )
        }
        val msg = json.encodeToString(PolymorphicSerializer(Message::class), handshake)
        ws.sendText(msg, true)
    }

    // -------------------------------------------------------------------------------------
    // 6.3 — Start battle
    // -------------------------------------------------------------------------------------

    /**
     * Sends a `start-game` command to the server.
     *
     * @param gameSetup the game configuration
     * @param botAddresses addresses of bots participating in the battle
     */
    fun startBattle(gameSetup: GameSetup, botAddresses: Set<BotAddress>) {
        requireConnected()
        val msg = json.encodeToString(
            PolymorphicSerializer(Message::class),
            StartGame(gameSetup, botAddresses) as Message
        )
        controllerWs!!.sendText(msg, true)
    }

    // -------------------------------------------------------------------------------------
    // 6.4 — Stop battle
    // -------------------------------------------------------------------------------------

    /** Sends a `stop-game` command to the server. */
    fun stopBattle() {
        if (!isConnected) return
        sendControllerMessage(StopGame)
    }

    // -------------------------------------------------------------------------------------
    // 6.5 — Pause
    // -------------------------------------------------------------------------------------

    /** Sends a `pause-game` command to the server. */
    fun pauseBattle() {
        requireConnected()
        sendControllerMessage(PauseGame)
    }

    // -------------------------------------------------------------------------------------
    // 6.6 — Resume
    // -------------------------------------------------------------------------------------

    /** Sends a `resume-game` command to the server. */
    fun resumeBattle() {
        requireConnected()
        sendControllerMessage(ResumeGame)
    }

    // -------------------------------------------------------------------------------------
    // 6.7 — Next turn
    // -------------------------------------------------------------------------------------

    /** Sends a `next-turn` command to the server (single-step while paused). */
    fun nextTurn() {
        requireConnected()
        sendControllerMessage(NextTurn)
    }

    // -------------------------------------------------------------------------------------
    // 6.8 — Bot policy update
    // -------------------------------------------------------------------------------------

    /**
     * Sends a `bot-policy-update` to the server to change per-bot behaviour flags.
     *
     * @param botId           the id of the target bot
     * @param breakpointEnabled enable or disable breakpoint mode for this bot;
     *                          `null` leaves the current setting unchanged
     * @param debuggingEnabled  enable or disable debug graphics for this bot;
     *                          `null` leaves the current setting unchanged
     */
    fun setBotPolicy(botId: Int, breakpointEnabled: Boolean? = null, debuggingEnabled: Boolean? = null) {
        requireConnected()
        sendControllerMessage(BotPolicyUpdate(botId, debuggingEnabled = debuggingEnabled, breakpointEnabled = breakpointEnabled))
    }

    // -------------------------------------------------------------------------------------
    // 6.9 — Debug mode
    // -------------------------------------------------------------------------------------

    /**
     * Sends `enable-debug-mode` to the server.
     * In debug mode the server pauses after each turn instead of auto-advancing,
     * letting the controller step turn-by-turn via [nextTurn].
     */
    fun enableDebugMode() {
        requireConnected()
        sendControllerMessage(EnableDebugMode)
    }

    /**
     * Sends `disable-debug-mode` to the server, returning to normal auto-advancing.
     * Equivalent to [resumeBattle], which also implicitly disables debug mode.
     */
    fun disableDebugMode() {
        requireConnected()
        sendControllerMessage(DisableDebugMode)
    }

    // -------------------------------------------------------------------------------------
    // 6.9 — BattleResults extraction
    // -------------------------------------------------------------------------------------

    companion object {
        private const val CONNECT_TIMEOUT_MS = 10_000L

        /**
         * Converts a [GameEndedEvent] results array into a [BattleResults].
         */
        fun toBattleResults(event: GameEndedEvent): BattleResults {
            return BattleResults(
                numberOfRounds = event.numberOfRounds,
                results = event.results.map { it.toBotResult() }
            )
        }

        private fun Results.toBotResult(): BotResult = BotResult(
            id = id,
            name = name,
            version = version,
            isTeam = isTeam ?: false,
            rank = rank,
            totalScore = totalScore,
            survival = survival,
            lastSurvivorBonus = lastSurvivorBonus,
            bulletDamage = bulletDamage,
            bulletKillBonus = bulletKillBonus,
            ramDamage = ramDamage,
            ramKillBonus = ramKillBonus,
            firstPlaces = firstPlaces,
            secondPlaces = secondPlaces,
            thirdPlaces = thirdPlaces,
        )
    }

    // -------------------------------------------------------------------------------------
    // Connection management
    // -------------------------------------------------------------------------------------

    /** Closes both WebSocket connections. */
    override fun close() {
        connected.set(false)
        closeWebSocket(observerWs, "Observer")
        closeWebSocket(controllerWs, "Controller")
        observerWs = null
        controllerWs = null
    }

    private fun closeWebSocket(ws: WebSocket?, name: String) {
        try {
            ws?.abort()
        } catch (e: Exception) {
            logger.log(Level.FINE, "Error closing $name WebSocket", e)
        }
    }

    private fun sendControllerMessage(message: Message) {
        val msg = json.encodeToString(PolymorphicSerializer(Message::class), message)
        controllerWs!!.sendText(msg, true)
    }

    private fun requireConnected() {
        if (!isConnected) {
            throw BattleException("Not connected to server")
        }
    }
}
