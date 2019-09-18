package dev.robocode.tankroyale.ui.desktop.client

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import dev.robocode.tankroyale.ui.desktop.model.*
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.util.Event
import dev.robocode.tankroyale.ui.desktop.util.Version
import java.net.URI

object Client : AutoCloseable {

    private val defaultUri = URI("ws://localhost:55000")

    // public events
    val onConnected = Event<Unit>()
    val onDisconnected = Event<Unit>()
    val onError = Event<Exception>()

    val onBotListUpdate = Event<BotListUpdate>()

    val onGameStarted = Event<GameStartedEvent>()
    val onGameEnded = Event<GameEndedEvent>()
    val onGameAborted = Event<GameAbortedEvent>()
    val onGamePaused = Event<GamePausedEvent>()
    val onGameResumed = Event<GameResumedEvent>()

    val onTickEvent = Event<TickEvent>()

    var currentGameSetup: GameSetup? = null

    private var websocket: WebSocketClient = WebSocketClient(defaultUri)

    private val json = Json(context = messageModule)

    private var games: Set<GameSetup> = HashSet()
    private var bots: Set<BotInfo> = HashSet()

    var isGameRunning: Boolean = false
        private set

    var isGamePaused: Boolean = false
        private set

    private var lastStartGame: StartGame? = null

    val isConnected: Boolean get() = websocket.isOpen()

    override fun close() {
        stopGame()

        if (isConnected) websocket.close()

        onDisconnected.publish(Unit)
    }

    fun connect(endpoint: String) {
        websocket = WebSocketClient(URI(endpoint))

        websocket.onOpen.subscribe { onConnected.publish(Unit) }
        websocket.onClose.subscribe { onDisconnected.publish(Unit) }
        websocket.onMessage.subscribe { onMessage(it) }
        websocket.onError.subscribe { onError.publish(it) }

        websocket.open() // must be called after onOpen.subscribe()
    }

    fun startGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>) {
        if (!isGameRunning && isConnected) {
            lastStartGame = StartGame(gameSetup, botAddresses)
            val startGame = lastStartGame
            websocket.send(startGame!!)
        }
    }

    fun stopGame() {
        if (isGameRunning && websocket.isOpen()) {
            websocket.send(StopGame())
        }
    }

    fun restartGame() {
        stopGame()
        val startGame = lastStartGame
        websocket.send(startGame!!)
    }

    fun pauseGame() {
        if (!isGamePaused) {
            websocket.send(PauseGame())
        }
    }

    fun resumeGame() {
        if (isGamePaused) {
            println("send ResumeGame()")
            websocket.send(ResumeGame())
        }
    }

    private fun onMessage(msg: String) {
        when (val type = json.parse(PolymorphicSerializer(Message::class), msg)) {
            is TickEvent -> handleTickEvent(type)
            is ServerHandshake -> handleServerHandshake(type)
            is BotListUpdate -> handleBotListUpdate(type)
            is GameStartedEvent -> handleGameStarted(type)
            is GameEndedEvent -> handleGameEnded(type)
            is GameAbortedEvent -> handleGameAborted(type)
            is GamePausedEvent -> handleGamePaused(type)
            is GameResumedEvent -> handleGameResumed(type)
            else -> throw IllegalArgumentException("Unknown content type: $type")
        }
    }

    private fun handleServerHandshake(serverHandshake: ServerHandshake) {
        games = serverHandshake.games

        val handshake = ControllerHandshake(
            name = "Robocode Tank Royale UI",
            version = "${Version.getVersion()}",
            author = "Flemming N. Larsen",
            secret = ServerProcess.secret
        )
        websocket.send(handshake)
    }

    private fun handleBotListUpdate(botListUpdate: BotListUpdate) {
        bots = botListUpdate.bots
        onBotListUpdate.publish(botListUpdate)
    }

    private fun handleGameStarted(gameStartedEvent: GameStartedEvent) {
        isGameRunning = true
        currentGameSetup = gameStartedEvent.gameSetup

        onGameStarted.publish(gameStartedEvent)
    }

    private fun handleGameEnded(gameEndedEvent: GameEndedEvent) {
        isGameRunning = false
        isGamePaused = false
        onGameEnded.publish(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        isGameRunning = false
        isGamePaused = false
        onGameAborted.publish(gameAbortedEvent)
    }

    private fun handleGamePaused(gamePausedEvent: GamePausedEvent) {
        println("handleGamePaused")
        isGamePaused = true
        onGamePaused.publish(gamePausedEvent)
    }

    private fun handleGameResumed(gameResumedEvent: GameResumedEvent) {
        println("handleGameResumed")
        isGamePaused = false
        onGameResumed.publish(gameResumedEvent)
    }

    private fun handleTickEvent(tickEvent: TickEvent) {
        onTickEvent.publish(tickEvent)
    }
}