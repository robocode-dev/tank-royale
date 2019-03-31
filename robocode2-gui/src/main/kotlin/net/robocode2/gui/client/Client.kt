package net.robocode2.gui.client

import com.beust.klaxon.Klaxon
import net.robocode2.gui.model.*
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Observable
import java.net.URI

object Client : AutoCloseable {

    val defaultUri = URI("ws://localhost:55000")

    // public events
    val onConnected = Observable<Unit>()
    val onDisconnected = Observable<Unit>()

    val onBotListUpdate = Observable<BotListUpdate>()

    val onGameStarted = Observable<GameStartedEvent>()
    val onGameEnded = Observable<GameEndedEvent>()
    val onGameAborted = Observable<GameAbortedEvent>()

    val onTickEvent = Observable<TickEvent>()

    private val disposables = ArrayList<Disposable>()

    private var websocket: WebSocketClient = WebSocketClient(defaultUri)

    private var clientKey: String? = null
    private var games: Set<GameSetup> = HashSet()
    private var bots: Set<BotInfo> = HashSet()

    var isGameRunning: Boolean = false

    override fun close() {
        abortGame()

        disposables.forEach { it.dispose() }
        disposables.clear()

        if (websocket.isOpen()) {
            websocket.close()
        }

        onDisconnected.notify(Unit)
    }

    fun connect(uri: URI) {
        websocket = WebSocketClient(uri)

        try {
            disposables.add(websocket.onOpen.subscribe { onConnected.notify(Unit) })
            disposables.add(websocket.onClose.subscribe { onDisconnected.notify(Unit) })
            disposables.add(websocket.onMessage.subscribe { onMessage(it) })

            websocket.open() // must be called after onOpen.subscribe()

        } catch (e: RuntimeException) {
            disposables.forEach { it.dispose() }
        }
    }

    fun isConnected() = websocket.isOpen()

    fun getAvailableBots(): Set<BotInfo> = bots

    fun startGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>) {
        if (!isGameRunning && websocket.isOpen()) {
            websocket.send(StartGame(gameSetup, botAddresses))
        }
    }

    fun abortGame() {
        if (isGameRunning && websocket.isOpen()) {
            websocket.send(AbortGame())
        }
    }

    private fun onMessage(msg: String) {
        val content = Klaxon().parse<Content>(msg)
        when (content) {
            is ServerHandshake -> handleServerHandshake(content)
            is BotListUpdate -> handleBotListUpdate(content)
            is GameStartedEvent -> handleGameStarted(content)
            is GameEndedEvent -> handleGameEnded(content)
            is GameAbortedEvent -> handleGameAborted(content)
            is TickEvent -> handleTickEvent(content)
            else -> throw IllegalArgumentException("Unknown content type: $content")
        }
    }

    private fun handleServerHandshake(serverHandshake: ServerHandshake) {
        clientKey = serverHandshake.clientKey
        games = serverHandshake.games

        val handshake = ControllerHandshake(
                clientKey = this.clientKey ?: throw IllegalStateException("client key cannot be null"),
                name = "Robocode 2 UI",
                version = "0.1",
                author = "Flemming N. Larsen"
        )
        websocket.send(handshake)
    }

    private fun handleBotListUpdate(botListUpdate: BotListUpdate) {
        bots = botListUpdate.bots
        onBotListUpdate.notify(botListUpdate)
    }

    private fun handleGameStarted(gameStartedEvent: GameStartedEvent) {
        isGameRunning = true
        onGameStarted.notify(gameStartedEvent)
    }

    private fun handleGameEnded(gameEndedEvent: GameEndedEvent) {
        isGameRunning = false
        onGameEnded.notify(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        isGameRunning = false
        onGameAborted.notify(gameAbortedEvent)
    }

    private fun handleTickEvent(tickEvent: TickEvent) {
        onTickEvent.notify(tickEvent)
    }
}