package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.gui.client.ClientEvents.onBotListUpdate
import dev.robocode.tankroyale.gui.client.ClientEvents.onConnected
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameAborted
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameEnded
import dev.robocode.tankroyale.gui.client.ClientEvents.onGamePaused
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameResumed
import dev.robocode.tankroyale.gui.client.ClientEvents.onGameStarted
import dev.robocode.tankroyale.gui.client.ClientEvents.onRoundEnded
import dev.robocode.tankroyale.gui.client.ClientEvents.onRoundStarted
import dev.robocode.tankroyale.gui.client.ClientEvents.onTickEvent
import dev.robocode.tankroyale.gui.model.*
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents
import dev.robocode.tankroyale.gui.util.Version
import kotlinx.serialization.PolymorphicSerializer
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

object Client {

    var currentGameSetup: GameSetup? = null

    var currentTick: TickEvent? = null

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

    init {
        TpsEvents.onTpsChanged.subscribe(Client) { changeTps(it.tps) }
        ClientEvents.onBotPolicyChanged.subscribe(Client) { changeBotPolicy(it) }

        ServerEvents.onStopped.subscribe(Client) {
            isRunning.set(false)
            isPaused.set(false)

            bots.clear()

            websocket?.let {
                if (it.isOpen()) {
                    it.close()
                }
                websocket = null
            }
        }
    }

    fun isConnected(): Boolean = websocket?.isOpen() ?: false

    fun connect() {
        check (!isConnected()) { "Websocket is already connected" }
        websocket = WebSocketClient(URI(ServerSettings.serverUrl()))

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

    fun close() {
        stopGame()

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

        savedStdOutput.clear()
        savedStdError.clear()
    }

    fun startGame(botAddresses: Set<BotAddress>) {
        savedStdOutput.clear()
        savedStdError.clear()

        if (isRunning.get()) {
            stopGame()
        }

        val displayName = ConfigSettings.gameType.displayName
        val gameSetup = GamesSettings.games[displayName]!!

        lastStartGame = StartGame(gameSetup.toGameSetup(), botAddresses)
        send(lastStartGame)
    }

    fun stopGame() {
        resumeGame()
        if (isRunning.get()) {
            send(StopGame)
        }
    }

    fun restartGame() {
        if (isRunning.get()) {
            val eventOwner = Object()
            onGameAborted.subscribe(eventOwner, true) {
                startWithLastGameSetup()
            }
            onGameEnded.subscribe(eventOwner, true) {
                startWithLastGameSetup()
            }
            stopGame()

        } else {
            startWithLastGameSetup()
        }
    }

    fun pauseGame() {
        if (isRunning.get() && !isPaused.get()) {
            send(PauseGame)
        }
    }

    fun resumeGame() {
        if (isRunning.get() && isPaused.get()) {
            send(ResumeGame)
        }
    }

    internal fun doNextTurn() {
        if (isRunning.get() && isPaused.get()) {
            send(NextTurn)
        }
    }

    fun isGameRunning(): Boolean = isRunning.get()
    fun isGamePaused(): Boolean = isPaused.get()

    val joinedBots: Set<BotInfo> get() = bots

    fun getParticipant(botId: Int): Participant = participants.first { participant -> participant.id == botId }

    fun getStandardOutput(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? = savedStdOutput[botId]

    fun getStandardError(botId: Int): Map<Int /* round */, Map<Int /* turn */, String>>? = savedStdError[botId]

    private fun startWithLastGameSetup() {
        send(lastStartGame)
    }

    private fun send(message: Message) {
        check(isConnected()) { "Websocket is not connected" }
        websocket?.send(message)
    }

    private fun changeTps(tps: Int) {
        if (isRunning.get() && tps != lastTps) {
            lastTps = tps
            send(ChangeTps(tps))
        }
    }

    private fun changeBotPolicy(botPolicyUpdate: BotPolicyUpdate) {
        send(botPolicyUpdate)
    }

    private fun onMessage(msg: String) {
//        println("msg: $msg")
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

        changeTps(ConfigSettings.tps)
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
            ClientEvents.onStdOutputUpdated.fire(tickEvent)
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