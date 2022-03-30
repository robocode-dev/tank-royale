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

object Client {

    init {
        TpsEvents.onTpsChanged.subscribe(Client) { changeTps(it.tps) }

        ServerEvents.onStopped.subscribe(Client) {
            isGamePaused = false
            isGameRunning = false

            bots.clear()
        }
    }

    var currentGameSetup: GameSetup? = null

    var isGameRunning: Boolean = false

    var isGamePaused: Boolean = false
        private set

    private val isConnected: Boolean get() = websocket?.isOpen() ?: false

    private var participants = listOf<Participant>()
    private var bots = HashSet<BotInfo>()

    val joinedBots: Set<BotInfo>
        get() { return bots }

    private var websocket: WebSocketClient? = null

    private val json = MessageConstants.json

    private var gameTypes = setOf<String>()

    private var lastStartGame: StartGame? = null

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

    fun close() {
        stopGame()

        if (isConnected) {
            websocket?.close()
            websocket = null
        }
    }

    fun startGame(botAddresses: Set<BotAddress>) {
        if (isGameRunning) {
            stopGame()
        }

        val displayName = ServerSettings.gameType.displayName
        val gameSetup = GamesSettings.games[displayName]!!

        lastStartGame = StartGame(gameSetup.toGameSetup(), botAddresses)
        send(lastStartGame!!)
    }

    fun stopGame() {
        if (isGameRunning) {
            send(StopGame())
        }
        isGamePaused = false
    }

    fun restartGame() {
        resumeGame()
        stopGame()

        send(lastStartGame!!)
    }

    fun pauseGame() {
        if (isGameRunning && !isGamePaused) {
            send(PauseGame())
        }
    }

    fun resumeGame() {
        if (isGameRunning && isGamePaused) {
            send(ResumeGame())
        }
    }

    fun getParticipant(id: Int): Participant = participants.first { participant -> participant.id == id }

    private fun send(message: Message) {
        if (!isConnected) throw IllegalStateException("Websocket is not connected")
        websocket!!.send(message)
    }

    private fun changeTps(tps: Int) {
        if (isGameRunning && tps != this.tps) {
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
        isGameRunning = true
        currentGameSetup = gameStartedEvent.gameSetup
        participants = gameStartedEvent.participants

        onGameStarted.fire(gameStartedEvent)
    }

    private fun handleGameEnded(gameEndedEvent: GameEndedEvent) {
        isGameRunning = false
        isGamePaused = false
        onGameEnded.fire(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        isGameRunning = false
        isGamePaused = false
        onGameAborted.fire(gameAbortedEvent)
    }

    private fun handleGamePaused(gamePausedEvent: GamePausedEvent) {
        isGamePaused = true
        onGamePaused.fire(gamePausedEvent)
    }

    private fun handleGameResumed(gameResumedEvent: GameResumedEvent) {
        isGamePaused = false
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