package net.robocode2.gui.server

import com.beust.klaxon.Klaxon
import net.robocode2.gui.model.Content
import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.model.TickEvent
import net.robocode2.gui.model.comm.*
import net.robocode2.gui.model.control.StartGame
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Observable
import java.net.URI

object Client {

    val defaultUri = URI("ws://localhost:50000")

    // public events
    val onConnected = Observable<Unit>()
    val onDisconnected = Observable<Unit>()
    val onBotListUpdate = Observable<BotListUpdate>()

    private val disposables = ArrayList<Disposable>()

    private var websocket: WebSocketClient = WebSocketClient(defaultUri)

    private var clientKey: String? = null
    private var games: Set<GameSetup> = HashSet()
    private var bots: Set<BotInfo> = HashSet()

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

    fun disconnect() {
        websocket.close()
        disposables.forEach { it.dispose() }
    }

    fun isConnected() = websocket.isOpen()

    fun getAvailableBots(): Set<BotInfo> = bots

    fun startGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>) {
        val startGame: StartGame = StartGame(clientKey ?: return, gameSetup, botAddresses)
        websocket.send(startGame)
    }

    private fun onMessage(msg: String) {
        val content = Klaxon().parse<Content>(msg)
        when (content) {
            is ServerHandshake -> handleServerHandshake(content)
            is BotListUpdate -> handleBotListUpdate(content)
            is TickEvent -> println("### TICK EVENT ###")
            else -> throw IllegalArgumentException("Unknown content type: $content")
        }
    }

    private fun handleServerHandshake(handshake: ServerHandshake) {
        clientKey = handshake.clientKey
        games = handshake.games

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
}