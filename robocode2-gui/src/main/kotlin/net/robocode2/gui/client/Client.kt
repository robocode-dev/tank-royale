package net.robocode2.gui.client

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import net.robocode2.gui.model.*
import net.robocode2.gui.utils.Event
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

    val onTickEvent = Event<TickEvent>()

    var currentGameSetup: GameSetup? = null

    private var websocket: WebSocketClient = WebSocketClient(defaultUri)

    private val json = Json(context = messageModule)

    private var games: Set<GameSetup> = HashSet()
    private var bots: Set<BotInfo> = HashSet()

    private var isGameRunning: Boolean = false

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

    fun getAvailableBots(): Set<BotInfo> = bots

    fun startGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>) {
        if (!isGameRunning && isConnected) {
            websocket.send(StartGame(gameSetup, botAddresses))
        }
    }

    fun stopGame() {
        if (isGameRunning && websocket.isOpen()) {
            websocket.send(StopGame())
        }
    }

    private fun onMessage(msg: String) {
        when (val type = json.parse(PolymorphicSerializer(Message::class), msg)) {
            is ServerHandshake -> handleServerHandshake(type)
            is BotListUpdate -> handleBotListUpdate(type)
            is GameStartedEvent -> handleGameStarted(type)
            is GameEndedEvent -> handleGameEnded(type)
            is GameAbortedEvent -> handleGameAborted(type)
            is TickEvent -> handleTickEvent(type)
            else -> throw IllegalArgumentException("Unknown content type: $type")
        }
    }

    private fun handleServerHandshake(serverHandshake: ServerHandshake) {
        games = serverHandshake.games

        val handshake = ControllerHandshake(
                name = "Robocode 2 UI",
                version = "0.1", // TODO from version.txt file?
                author = "Flemming N. Larsen"
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
        onGameEnded.publish(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        isGameRunning = false
        onGameAborted.publish(gameAbortedEvent)
    }

    private fun handleTickEvent(tickEvent: TickEvent) {
        onTickEvent.publish(tickEvent)
    }
}