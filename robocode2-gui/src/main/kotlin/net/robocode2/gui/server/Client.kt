package net.robocode2.gui.server

import com.beust.klaxon.Klaxon
import net.robocode2.gui.model.Content
import net.robocode2.gui.model.GameSetup
import net.robocode2.gui.model.TickEvent
import net.robocode2.gui.model.comm.BotAddress
import net.robocode2.gui.model.comm.BotListUpdate
import net.robocode2.gui.model.comm.ServerHandshake
import net.robocode2.gui.model.control.StartGame
import net.robocode2.gui.utils.Disposable
import net.robocode2.gui.utils.Observable
import net.robocode2.schema.comm.ControllerHandshake
import java.net.URI

object Client {

    val defaultUri = URI("ws://localhost:50000")

    // public events
    val onConnected = Observable<Unit>()
    val onDisconnected = Observable<Unit>()

    // private events
    private val onServerHandshake = Observable<ServerHandshake>()

    private val disposables = ArrayList<Disposable>()

    private var clientKey: String? = null
    private var games: Set<GameSetup> = HashSet()

    private var websocket: WebSocketClient = WebSocketClient(defaultUri)

    fun connect(uri: URI) {
        websocket = WebSocketClient(uri)

        try {
            disposables.add(websocket.onOpen.subscribe { onConnected.notify(Unit) })
            disposables.add(websocket.onClose.subscribe { onDisconnected.notify(Unit) })
            disposables.add(websocket.onMessage.subscribe { onMessage(it) })
            disposables.add(onServerHandshake.subscribe { onServerHandshake(it) })

            websocket.open() // must be called after onOpen.subscribe()
        } finally {
            disposables.forEach { it.dispose() }
        }
    }

    fun disconnect() {
        websocket.close()
        disposables.forEach { it.dispose() }
    }

    fun isConnected() = websocket.isOpen()

    fun startGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>) {
        websocket.send(StartGame(gameSetup, botAddresses))
    }

    private fun onMessage(msg: String) {
        val content = Klaxon().parse<Content>(msg)
        when (content) {
            is ServerHandshake -> onServerHandshake.notify(content)
            is BotListUpdate -> println("### BOT LIST UPDATE ###")
            is TickEvent -> println("### TICK EVENT ###")
            else -> throw IllegalArgumentException("Unknown content type: $content")
        }
    }

    private fun onServerHandshake(handshake: ServerHandshake) {
        clientKey = handshake.clientKey
        games = handshake.games

        sendHandshake()
    }

    private fun sendHandshake() {
        val handshake = ControllerHandshake()
        handshake.name = "Robocode 2 UI"
        handshake.version = "0.1"
        handshake.author = "Flemming N. Larsen"
        websocket.send(handshake)
    }
}