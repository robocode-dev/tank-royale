package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.core.ServerSetup
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core.StatusCode
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConnectionHandler(
    private val setup: ServerSetup,
    private val listener: IConnectionListener,
    private val controllerSecrets: Set<String>,
    private val botSecrets: Set<String>
) {
    private val webSocketObserver: WebSocketObserver
    private val allConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val botConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val observerConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val controllerConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val botHandshakes = ConcurrentHashMap<WebSocket, BotHandshake>()
    private val observerHandshakes = ConcurrentHashMap<WebSocket, ObserverHandshake>()
    private val controllerHandshakes = ConcurrentHashMap<WebSocket, ControllerHandshake>()
    private val executorService: ExecutorService

    private val log = LoggerFactory.getLogger(ConnectionHandler::class.java)

    private val gson = Gson()

    init {
        val address = InetSocketAddress("localhost", Server.port.toInt())
        webSocketObserver = WebSocketObserver(address).apply {
            isTcpNoDelay = true
        }
        executorService = Executors.newCachedThreadPool()
    }

    fun start() {
        webSocketObserver.run()
    }

    fun stop() {
        shutdownAndAwaitTermination(executorService)
    }

    fun broadcastToObserverAndControllers(message: String) {
        broadcast(observerAndControllerConnections, message)
    }

    fun getBotConnections(): Set<WebSocket> = botConnections.toSet()

    val observerAndControllerConnections: Set<WebSocket>
        get() =
            mutableSetOf<WebSocket>().apply {
                this += observerConnections
                this += controllerConnections
            }

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> = botHandshakes.toMap()

    fun getBotConnections(botAddresses: Collection<BotAddress>): Set<WebSocket> =
        mutableSetOf<WebSocket>().apply {
            botHandshakes.keys.forEach { conn ->
                addToFoundConnection(conn, botAddresses, this)
            }
        }

    private fun addToFoundConnection(
        conn: WebSocket,
        botAddresses: Collection<BotAddress>,
        foundConnections: MutableSet<WebSocket>
    ) {
        conn.remoteSocketAddress?.let { address ->
            botAddresses.forEach { botAddress ->
                if (toIpAddress(address) == toIpAddress(address) && botAddress.port == address.port) {
                    foundConnections += conn
                    return@forEach
                }
            }
        }
    }

    private fun toIpAddress(address: InetSocketAddress): String {
        val ip = address.toString().split("/")[1]
        return if (ip.equals("localhost", true)) "127.0.0.1" else ip
    }

    private fun shutdownAndAwaitTermination(pool: ExecutorService) {
        pool.apply {
            shutdown() // Disable new tasks from being submitted
            try {
                if (!awaitTermination(5, TimeUnit.SECONDS)) {
                    shutdownNow()
                    if (!awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("Pool did not terminate")
                    }
                }
            } catch (ex: InterruptedException) {
                shutdownNow()
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun send(conn: WebSocket, message: String) {
        log.debug("Sending to: ${conn.remoteSocketAddress}, message: $message")
        conn.send(message)
    }

    fun broadcast(clients: Collection<WebSocket>, message: String) {
        log.debug("Broadcast message: $message")
        webSocketObserver.broadcast(message, clients)
    }

    private fun notifyException(exception: Exception) {
        log.error("Exception occurred: $exception")
        listener.onException(exception)
    }

    private inner class WebSocketObserver(address: InetSocketAddress) : WebSocketServer(address) {

        override fun onStart() {}

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            log.debug("onOpen(): ${conn.remoteSocketAddress}")

            executorService.submit {
                allConnections += conn
                ServerHandshake().apply {
                    `$type` = Message.`$type`.SERVER_HANDSHAKE
                    variant = "Tank Royale"
                    version = version ?: "?"
                    gameTypes = setup.gameTypes
                }.also {
                    send(conn, Gson().toJson(it))
                }
            }
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            log.debug("onClose: ${conn.remoteSocketAddress}, code: $code, reason: $reason, remote: $remote")

            executorService.submit {
                allConnections -= conn
                when {
                    botConnections.remove(conn) -> handleBotLeft(conn)
                    observerConnections.remove(conn) -> handleObserverLeft(conn)
                    controllerConnections.remove(conn) -> handleControllerLeft(conn)
                }
            }
        }

        private fun handleBotLeft(conn: WebSocket) {
            botHandshakes[conn]?.let {
                listener.onBotLeft(conn, it)
            }
            botHandshakes -= conn
        }

        private fun handleObserverLeft(conn: WebSocket) {
            observerHandshakes[conn]?.let {
                listener.onObserverLeft(conn, it)
            }
            observerHandshakes -= conn
        }

        private fun handleControllerLeft(conn: WebSocket) {
            controllerHandshakes[conn]?.let {
                listener.onControllerLeft(conn, it)
            }
            controllerHandshakes -= conn
        }

        override fun onMessage(conn: WebSocket, message: String) {
            log.debug("onMessage: ${conn.remoteSocketAddress}, message: $message")

            executorService.submit {
                try {
                    gson.fromJson(message, JsonObject::class.java)["\$type"]?.let { jsonType ->
                        try {
                            val type = Message.`$type`.fromValue(jsonType.asString)

                            log.debug("Handling message: $type")
                            when (type) {
                                Message.`$type`.BOT_INTENT -> handleIntent(conn, message)
                                Message.`$type`.BOT_HANDSHAKE -> handleBotHandshake(conn, message)
                                Message.`$type`.OBSERVER_HANDSHAKE -> handleObserverHandshake(conn, message)
                                Message.`$type`.CONTROLLER_HANDSHAKE -> handleControllerHandshake(conn, message)
                                Message.`$type`.BOT_READY -> handleBotReady(conn)
                                Message.`$type`.START_GAME -> handleStartGame(message)
                                Message.`$type`.STOP_GAME -> handleStopGame()
                                Message.`$type`.PAUSE_GAME -> handlePauseGame()
                                Message.`$type`.RESUME_GAME -> handleResumeGame()
                                Message.`$type`.CHANGE_TPS -> handleChangeTps(message)
                                else -> notifyException(IllegalStateException("Unhandled message type: $type"))
                            }
                        } catch (ex: IllegalArgumentException) {
                            notifyException(IllegalStateException("Unhandled message type: ${jsonType.asString}"))
                        }
                    }
                } catch (ex: JsonSyntaxException) {
                    log.error("Invalid message: $message", ex)
                } catch (ex: Exception) {
                    log.error("Error when passing message: $message", ex)
                }
            }
        }

        override fun onError(conn: WebSocket, ex: Exception) {
            log.error("onError: ${conn.remoteSocketAddress}, exception: $ex")
        }
    }

    private fun handleIntent(conn: WebSocket, message: String) {
        val intent = gson.fromJson(message, BotIntent::class.java)
        listener.onBotIntent(conn, botHandshakes[conn]!!, intent)
    }

    private fun handleBotHandshake(conn: WebSocket, message: String) {
        gson.fromJson(message, BotHandshake::class.java).apply {
            // Validate client secret before continuing
            if (botSecrets.isNotEmpty() && !botSecrets.contains(secret)) {
                log.info("Ignoring bot using invalid secret: name: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Wrong secret")
            } else {
                botConnections += conn
                botHandshakes[conn] = this
                listener.onBotJoined(conn, this)
            }
        }
    }

    private fun handleObserverHandshake(conn: WebSocket, message: String) {
        gson.fromJson(message, ObserverHandshake::class.java).apply {
            // Validate client secret before continuing
            if (controllerSecrets.isNotEmpty() && !controllerSecrets.contains(secret)) {
                log.info("Ignoring observer using invalid secret: name: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Wrong secret")
            } else {
                observerConnections += conn
                observerHandshakes[conn] = this
                listener.onObserverJoined(conn, this)
            }
        }
    }

    private fun handleControllerHandshake(conn: WebSocket, message: String) {
        gson.fromJson(message, ControllerHandshake::class.java).apply {
            // Validate client secret before continuing
            if (controllerSecrets.isNotEmpty() && !controllerSecrets.contains(secret)) {
                log.info("Ignoring controller using invalid secret: name: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Wrong secret")
            } else {
                controllerConnections += conn
                controllerHandshakes[conn] = this
                listener.onControllerJoined(conn, this)
            }
        }
    }

    private fun handleBotReady(conn: WebSocket) {
        listener.onBotReady(conn, botHandshakes[conn]!!)
    }

    private fun handleStartGame(message: String) {
        gson.fromJson(message, StartGame::class.java).apply {
            listener.onStartGame(gameSetup, botAddresses)
        }
    }

    private fun handleStopGame() {
        executorService.submit(listener::onAbortGame)
    }

    private fun handlePauseGame() {
        executorService.submit(listener::onPauseGame)
    }

    private fun handleResumeGame() {
        executorService.submit(listener::onResumeGame)
    }

    private fun handleChangeTps(message: String) {
        executorService.submit {
            gson.fromJson(message, ChangeTps::class.java).apply {
                listener.onChangeTps(tps)
            }
        }
    }
}