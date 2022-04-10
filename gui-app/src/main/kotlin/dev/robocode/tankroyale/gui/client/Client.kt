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
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.tps.TpsEvents
import dev.robocode.tankroyale.gui.util.Version
import kotlinx.serialization.PolymorphicSerializer
import java.net.URI
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object Client {

    init {
        TpsEvents.onTpsChanged.subscribe(Client) { changeTps(it.tps) }

        ServerEvents.onStopped.subscribe(Client) {
            isRunning.set(false)
            isPaused.set(false)

            bots.clear()
        }
    }

    var currentGameSetup: GameSetup? = null

    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    private val isConnected: Boolean get() = websocket?.isOpen() ?: false

    private var participants = listOf<Participant>()
    private var bots = HashSet<BotInfo>()

    val joinedBots: Set<BotInfo>
        get() { return bots }

    private var websocket: WebSocketClient? = null

    private val json = MessageConstants.json

    private var gameTypes = setOf<String>()

    private lateinit var lastStartGame: StartGame

    private var tps: Int? = null

    fun connect() {
        if (isConnected) {
            throw IllegalStateException("Websocket is already connected")
        }
        val websocket = WebSocketClient(URI(ServerSettings.serverUrl))
        this.websocket = websocket

        WebSocketClientEvents.apply {
            onOpen.subscribe(websocket) { onConnected.fire(Unit) }
            onMessage.subscribe(websocket) { onMessage(it) }
            onError.subscribe(websocket) { System.err.println("WebSocket error: " + it.message) }

            websocket.open() // must be called after onOpen.subscribe()
        }
    }

    fun isGameRunning(): Boolean = isRunning.get()
    fun isGamePaused(): Boolean = isPaused.get()

    fun close() {
        stopGame()

        if (isConnected) {
            websocket?.close()
            websocket = null
        }
    }

    fun startGame(botAddresses: Set<BotAddress>) {
        if (isRunning.get()) {
            stopGame()
        }

        val displayName = ServerSettings.gameType.displayName
        val gameSetup = GamesSettings.games[displayName]!!

        lastStartGame = StartGame(gameSetup.toGameSetup(), botAddresses)
        send(lastStartGame)
    }

    fun stopGame() {
        resumeGame()
        if (isRunning.get()) {
            send(StopGame())
        }
    }

    fun restartGame() {
        val eventOwner = Object()
        onGameAborted.subscribe(eventOwner, true) {
            startWithLastGameSetup()
        }
        onGameEnded.subscribe(eventOwner, true) {
            startWithLastGameSetup()
        }
        stopGame()
    }

    fun pauseGame() {
        if (isRunning.get() && !isPaused.get()) {
            send(PauseGame())
        }
    }

    fun resumeGame() {
        if (isRunning.get() && isPaused.get()) {
            send(ResumeGame())
        }
    }

    fun getParticipant(id: Int): Participant = participants.first { participant -> participant.id == id }

    private fun startWithLastGameSetup() {
        send(lastStartGame)
    }

    private fun send(message: Message) {
        if (!isConnected) throw IllegalStateException("Websocket is not connected")
        websocket?.send(message)

        println("send: $message")
    }

    private fun changeTps(tps: Int) {
        if (isRunning.get() && tps != this.tps) {
            this.tps = tps
            send(ChangeTps(tps))
        }
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
            is TpsChangedEvent -> handleTpsChanged(type)
            else -> throw IllegalArgumentException("Unknown content type: $type")
        }
    }

    private fun handleServerHandshake(serverHandshake: ServerHandshake) {
        gameTypes = serverHandshake.gameTypes

        val handshake = ControllerHandshake(
            name = "Robocode Tank Royale UI",
            version = "${Version.version}",
            author = "Flemming N. Larsen",
            secret = ServerSettings.controllerSecrets.first()
        )
        send(handshake)
    }

    private fun handleBotListUpdate(botListUpdate: BotListUpdate) {
        bots = HashSet(botListUpdate.bots)
        onBotListUpdate.fire(botListUpdate)
    }

    private fun handleGameStarted(gameStartedEvent: GameStartedEvent) {
        println("->game started")
        isRunning.set(true)
        currentGameSetup = gameStartedEvent.gameSetup
        participants = gameStartedEvent.participants

        onGameStarted.fire(gameStartedEvent)
    }

    private fun handleGameEnded(gameEndedEvent: GameEndedEvent) {
        println("->game ended")
        isRunning.set(false)
        isPaused.set(false)
        onGameEnded.fire(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        println("->game aborted")
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
        onTickEvent.fire(tickEvent)
    }

    private fun handleTpsChanged(tpsChangedEvent: TpsChangedEvent) {
        TpsEvents.onTpsChanged.fire(tpsChangedEvent)
    }
}