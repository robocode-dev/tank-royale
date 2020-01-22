package dev.robocode.tankroyale.ui.desktop.client

import dev.robocode.tankroyale.ui.desktop.model.*
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.util.Event
import dev.robocode.tankroyale.ui.desktop.util.RegisterWsProtocolCommand
import dev.robocode.tankroyale.ui.desktop.util.Version
import kotlinx.serialization.PolymorphicSerializer
import java.io.Closeable
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

object Client : AutoCloseable {

    init {
        RegisterWsProtocolCommand().execute()
    }

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

    private var isGameRunning: Boolean = false

    var isGamePaused: Boolean = false
        private set

    private val isConnected: Boolean get() = websocket.isOpen()

    private var bots: Set<BotInfo> = HashSet()

    val availableBots: Set<BotInfo>
        get() {
            return bots
        }

    private var websocket: WebSocketClient = WebSocketClient(URI(ServerSettings.defaultUrl))

    private val json = MessageConstants.Json

    private var gameTypes: Set<String> = HashSet()

    private val disposables = ArrayList<Closeable>()

    private var lastStartGame: StartGame? = null


    override fun close() {
        stopGame()

        if (isConnected) websocket.close()

        onDisconnected.publish(Unit)
    }

    fun connect(url: String) {
        disposables.forEach { it.close() }

        websocket = WebSocketClient(URI(url))

        disposables += websocket.onOpen.subscribe { onConnected.publish(Unit) }
        disposables += websocket.onClose.subscribe { onDisconnected.publish(Unit) }
        disposables += websocket.onMessage.subscribe { onMessage(it) }
        disposables += websocket.onError.subscribe { onError.publish(it) }

        websocket.open() // must be called after onOpen.subscribe()
    }

    fun startGame(gameSetup: IGameSetup, botAddresses: Set<BotAddress>) {
        if (isGameRunning) {
            stopGame()
        }
        if (isConnected) {
            lastStartGame = StartGame(gameSetup.toGameSetup(), botAddresses)
            val startGame = lastStartGame
            websocket.send(startGame!!)
        }
    }

    fun stopGame() {
        if (isGameRunning && websocket.isOpen()) {
            websocket.send(StopGame())
        }
        isGamePaused = false
    }

    fun restartGame() {
        stopGame()
        websocket.send(lastStartGame!!)
    }

    fun pauseGame() {
        if (isGameRunning && !isGamePaused) {
            websocket.send(PauseGame())
        }
    }

    fun resumeGame() {
        if (isGameRunning && isGamePaused) {
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
        gameTypes = serverHandshake.gameTypes

        val handshake = ControllerHandshake(
            name = "Robocode Tank Royale UI",
            version = "${Version.getVersion()}",
            author = "Flemming N. Larsen",
            secret = ServerProcess.secret
        )
        websocket.send(handshake)
    }

    private fun handleBotListUpdate(botListUpdate: BotListUpdate) {
        bots = Collections.unmodifiableSet(botListUpdate.bots)
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
        isGamePaused = true
        onGamePaused.publish(gamePausedEvent)
    }

    private fun handleGameResumed(gameResumedEvent: GameResumedEvent) {
        isGamePaused = false
        onGameResumed.publish(gameResumedEvent)
    }

    private fun handleTickEvent(tickEvent: TickEvent) {
        onTickEvent.publish(tickEvent)
    }
}