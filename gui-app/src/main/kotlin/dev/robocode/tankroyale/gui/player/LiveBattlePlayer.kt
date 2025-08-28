package dev.robocode.tankroyale.gui.player

import dev.robocode.tankroyale.client.WebSocketClient
import dev.robocode.tankroyale.client.WebSocketClientEvents
import dev.robocode.tankroyale.client.model.*
import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import kotlinx.serialization.PolymorphicSerializer
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


/**
 * Battle player implementation for live battles connecting to a Tank Royale server via WebSocket.
 */
class LiveBattlePlayer : BattlePlayer {

    private var currentGameSetup: GameSetup? = null
    private var currentTick: TickEvent? = null

    private val serverUrl = AtomicReference(ServerSettings.serverUrl())
    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    private var participants = listOf<Participant>()
    private var bots = mutableSetOf<BotInfo>()

    private var websocket: WebSocketClient? = null

    private val json = MessageConstants.json

    private var gameTypes = setOf<String>()

    private lateinit var lastStartGame: StartGame

    private var lastTps: Int? = null

    private val savedStdOutput =
        mutableMapOf<Int /* BotId */, MutableMap<Int /* round */, MutableMap<Int /* turn */, String>>>()
    private val savedStdError =
        mutableMapOf<Int /* BotId */, MutableMap<Int /* round */, MutableMap<Int /* turn */, String>>>()

    // Events
    override val onConnected = Event<Unit>()
    override val onGameStarted = Event<GameStartedEvent>()
    override val onGameEnded = Event<GameEndedEvent>()
    override val onGameAborted = Event<GameAbortedEvent>()
    override val onGamePaused = Event<GamePausedEvent>()
    override val onGameResumed = Event<GameResumedEvent>()
    override val onRoundStarted = Event<RoundStartedEvent>()
    override val onRoundEnded = Event<RoundEndedEvent>()
    override val onTickEvent = Event<TickEvent>()
    override val onBotListUpdate = Event<BotListUpdate>()
    override val onStdOutputUpdated = Event<TickEvent>()
    override val onSeekToTurn = Event<TickEvent>()

    init {
        ServerEvents.onStopped.subscribe(this) {
            if (isRunning.get()) {
                stop()
            }
            close()
        }
    }

    override fun start() {
        connect()
    }

    fun startGame(botAddresses: Set<BotAddress>) {
        savedStdOutput.clear()
        savedStdError.clear()

        if (isRunning.get()) {
            stop()
        }

        val displayName = ConfigSettings.gameType.displayName
        val gameSetup = GamesSettings.games[displayName]!!

        lastStartGame = StartGame(gameSetup.toGameSetup(), botAddresses)
        send(lastStartGame)
    }

    override fun stop() {
        resume()
        if (isRunning.get()) {
            send(StopGame)
        }
    }

    override fun pause() {
        if (isRunning.get() && !isPaused.get()) {
            send(PauseGame)
        }
    }

    override fun resume() {
        if (isRunning.get() && isPaused.get()) {
            send(ResumeGame)
        }
    }

    override fun nextTurn() {
        if (isRunning.get() && isPaused.get()) {
            send(NextTurn)
        }
    }

    override fun restart() {
        if (isRunning.get()) {
            val eventOwner = Object()
            onGameAborted.subscribe(eventOwner, true) {
                startWithLastGameSetup()
            }
            onGameEnded.subscribe(eventOwner, true) {
                startWithLastGameSetup()
            }
            stop()
        } else {
            startWithLastGameSetup()
        }
    }

    override fun isRunning(): Boolean = isRunning.get()

    override fun isPaused(): Boolean = isPaused.get()

    override fun getCurrentGameSetup(): GameSetup? = currentGameSetup

    override fun getCurrentTick(): TickEvent? = currentTick

    fun isCorrectlyConnected(): Boolean = serverUrl.get() == ServerSettings.serverUrl() && isConnected()

    private fun isConnected(): Boolean = websocket?.isOpen() ?: false

    override fun getJoinedBots(): Set<BotInfo> = bots

    override fun getParticipant(botId: Int): Participant = participants.first { participant -> participant.id == botId }

    override fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? =
        savedStdOutput[botId]

    override fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? = savedStdError[botId]

    fun connect() {
        val url = ServerSettings.serverUrl()
        if (url != serverUrl.get()) {
            close()
        }
        if (!isConnected()) {
            serverUrl.set(url)
            websocket = WebSocketClient(URI(url))

            WebSocketClientEvents.apply {
                websocket?.let { ws ->
                    onOpen.subscribe(ws) { onConnected.fire(Unit) }
                    onMessage.subscribe(ws) { onMessage(it) }
                    onError.subscribe(ws) {
                        System.err.println("WebSocket error: " + it.message)
                        ServerEvents.onStopped.fire(Unit)
                    }
                    try {
                        ws.open() // must be called AFTER onOpen.subscribe()
                    } catch (_: Exception) {
                        // to prevent redundant subscriptions which are kept both on failure, and
                        // new attempt to open the web socket
                        onOpen.unsubscribe(ws)
                        onMessage.unsubscribe(ws)
                        onError.unsubscribe(ws)
                    }
                }
            }
        }
    }

    fun close() {
        if (isConnected()) {
            WebSocketClientEvents.apply {
                websocket?.let { ws ->
                    onOpen.unsubscribe(ws)
                    onMessage.unsubscribe(ws)
                    onError.unsubscribe(ws)
                    ws.close()
                }
            }
            websocket = null
        }

        bots.clear()
        savedStdOutput.clear()
        savedStdError.clear()

        isRunning.set(false)
        isPaused.set(false)
    }

    private fun startWithLastGameSetup() {
        send(lastStartGame)
    }

    private fun send(message: Message) {
        check(isConnected()) { "WebSocket is not connected" }
        websocket?.send(message)
    }

    override fun changeTps(tps: Int) {
        if (isRunning.get() && tps != lastTps) {
            lastTps = tps
            send(ChangeTps(tps))
        }
    }

    override fun changeBotPolicy(botPolicyUpdate: BotPolicyUpdate) {
        send(botPolicyUpdate)
    }

    private fun onMessage(msg: String) {
        when (val type = json.decodeFromString(PolymorphicSerializer(Message::class), msg)) {
            is TickEvent -> handleTickEvent(type)
            is ServerHandshake -> handleServerHandshake(type)
            is BotListUpdate -> handleBotListUpdate(type)
            is GameStartedEvent -> handleGameStarted(type)
            is GameEndedEvent -> handleGameEnded(type)
            is GameAbortedEvent -> handleGameAborted(type)
            is GamePausedEvent -> handleGamePaused(type)
            is GameResumedEvent -> handleGameResumed(type)
            is RoundStartedEvent -> handleRoundStarted(type)
            is RoundEndedEvent -> handleRoundEnded(type)
            is TpsChangedEvent -> {
                // do nothing to prevent TPS change loop between server and client
            }

            else -> throw IllegalArgumentException("Unknown content type: $type")
        }
    }

    private fun handleServerHandshake(serverHandshake: ServerHandshake) {
        gameTypes = serverHandshake.gameTypes

        val handshake = ControllerHandshake(
            sessionId = serverHandshake.sessionId,
            name = "Robocode Tank Royale UI",
            version = "${Version.version}",
            author = "Flemming N. Larsen",
            secret = ServerSettings.controllerSecret()
        )
        send(handshake)
    }

    private fun handleBotListUpdate(botListUpdate: BotListUpdate) {
        bots = HashSet(botListUpdate.bots)
        onBotListUpdate.fire(botListUpdate)
    }

    private fun handleGameStarted(gameStartedEvent: GameStartedEvent) {
        isRunning.set(true)
        currentGameSetup = gameStartedEvent.gameSetup
        participants = gameStartedEvent.participants

        onGameStarted.fire(gameStartedEvent)
    }

    private fun handleGameEnded(gameEndedEvent: GameEndedEvent) {
        isRunning.set(false)
        isPaused.set(false)
        onGameEnded.fire(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        isRunning.set(false)
        isPaused.set(false)
        onGameAborted.fire(gameAbortedEvent)
    }

    private fun handleGamePaused(gamePausedEvent: GamePausedEvent) {
        isPaused.set(true)
        onGamePaused.fire(gamePausedEvent)
    }

    private fun handleGameResumed(gameResumedEvent: GameResumedEvent) {
        isPaused.set(false)
        onGameResumed.fire(gameResumedEvent)
    }

    private fun handleRoundStarted(roundStartedEvent: RoundStartedEvent) {
        onRoundStarted.fire(roundStartedEvent)
    }

    private fun handleRoundEnded(roundEndedEvent: RoundEndedEvent) {
        onRoundEnded.fire(roundEndedEvent)
    }

    private fun handleTickEvent(tickEvent: TickEvent) {
        currentTick = tickEvent

        onTickEvent.fire(tickEvent)

        updateSavedStdOutput(tickEvent)
    }

    private fun updateSavedStdOutput(tickEvent: TickEvent) {
        tickEvent.apply {
            botStates.forEach { botState ->
                val id = botState.id
                botState.stdOut?.let { updateStandardOutput(savedStdOutput, id, roundNumber, turnNumber, it) }
                botState.stdErr?.let { updateStandardOutput(savedStdError, id, roundNumber, turnNumber, it) }
            }
            onStdOutputUpdated.fire(tickEvent)
        }
    }

    private fun updateStandardOutput(
        stdOutputMaps: MutableMap<Int /* BotId */, MutableMap<Int /* round */, MutableMap<Int /* turn */, String>>>,
        id: Int, round: Int, turn: Int, output: String
    ) {
        stdOutputMaps
            .getOrPut(id) { LinkedHashMap() }
            .getOrPut(round) { LinkedHashMap() }[turn] = output
    }
}
