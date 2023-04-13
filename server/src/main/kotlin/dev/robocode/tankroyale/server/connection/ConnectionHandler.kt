package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.core.ServerSetup
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core.StatusCode
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.*
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
    private val sessionIds = ConcurrentHashMap<WebSocket, String /* sessionId */>()
    private val botHandshakes = ConcurrentHashMap<WebSocket, BotHandshake>()
    private val observerHandshakes = ConcurrentHashMap<WebSocket, ObserverHandshake>()
    private val controllerHandshakes = ConcurrentHashMap<WebSocket, ControllerHandshake>()
    private val executorService: ExecutorService

    private val log = LoggerFactory.getLogger(ConnectionHandler::class.java)

    private val gson = Gson()

    init {
        val address = InetSocketAddress("localhost", Server.port)
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
        get() = setOf<WebSocket>().plus(observerConnections).plus(controllerConnections)

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

    fun send(conn: WebSocket, message: String) {
        log.debug("Sending to: ${conn.remoteSocketAddress}, message: $message")
        try {
            conn.send(message)
        } catch (e: WebsocketNotConnectedException) {
            closeConnection(conn)
        }
    }

    fun broadcast(clients: Collection<WebSocket>, message: String) {
        log.debug("Broadcast message: $message")
        webSocketObserver.broadcast(message, clients)
    }

    private fun notifyException(exception: Exception) {
        log.error("Exception occurred: $exception")
        listener.onException(exception)
    }

    private fun closeConnection(conn: WebSocket) {
        allConnections -= conn
        when {
            botConnections.remove(conn) -> handleBotLeft(conn)
            observerConnections.remove(conn) -> handleObserverLeft(conn)
            controllerConnections.remove(conn) -> handleControllerLeft(conn)
        }
        sessionIds.remove(conn)
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

    private inner class WebSocketObserver(address: InetSocketAddress) : WebSocketServer(address) {

        override fun onStart() {}

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            log.debug("onOpen(): ${conn.remoteSocketAddress}")

            executorService.submit {
                allConnections += conn
                ServerHandshake().apply {
                    type = Message.Type.SERVER_HANDSHAKE
                    name = "Robocode Tank Royale server"
                    sessionId = generateAndStoreSessionId(conn)
                    variant = "Tank Royale"
                    version = dev.robocode.tankroyale.server.version.getVersion() ?: "?"
                    gameTypes = setup.gameTypes
                }.also {
                    send(conn, Gson().toJson(it))
                }
            }
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            log.debug("onClose: ${conn.remoteSocketAddress}, code: $code, reason: $reason, remote: $remote")

            executorService.submit {
                closeConnection(conn)
            }
        }

        override fun onMessage(conn: WebSocket, message: String) {
            log.debug("onMessage: ${conn.remoteSocketAddress}, message: $message")

            executorService.submit {
                try {
                    gson.fromJson(message, JsonObject::class.java)["type"]?.let { jsonType ->
                        try {
                            val type = Message.Type.fromValue(jsonType.asString)

                            log.debug("Handling message: $type")
                            when (type) {
                                Message.Type.BOT_INTENT -> handleIntent(conn, message)
                                Message.Type.BOT_HANDSHAKE -> handleBotHandshake(conn, message)
                                Message.Type.OBSERVER_HANDSHAKE -> handleObserverHandshake(conn, message)
                                Message.Type.CONTROLLER_HANDSHAKE -> handleControllerHandshake(conn, message)
                                Message.Type.BOT_READY -> handleBotReady(conn)
                                Message.Type.START_GAME -> handleStartGame(message)
                                Message.Type.STOP_GAME -> handleStopGame()
                                Message.Type.PAUSE_GAME -> handlePauseGame()
                                Message.Type.RESUME_GAME -> handleResumeGame()
                                Message.Type.NEXT_TURN -> handleNextTurn()
                                Message.Type.CHANGE_TPS -> handleChangeTps(message)
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

    private fun generateAndStoreSessionId(conn: WebSocket): String {
        val sessionId = generateSessionId()
        if (sessionIds.values.contains(sessionId)) {
            throw IllegalStateException("Generated session id has been generated before. It must be unique")
        }
        sessionIds[conn] = sessionId
        return sessionId
    }

    private fun generateSessionId(): String {
        val uuid = UUID.randomUUID()
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(uuid.mostSignificantBits)
        byteBuffer.putLong(uuid.leastSignificantBits)
        return Base64.getEncoder().withoutPadding().encodeToString(byteBuffer.array())
    }

    private fun handleIntent(conn: WebSocket, message: String) {
        val intent = gson.fromJson(message, BotIntent::class.java)
        listener.onBotIntent(conn, botHandshakes[conn]!!, intent)
    }

    private fun handleBotHandshake(conn: WebSocket, message: String) {
        gson.fromJson(message, BotHandshake::class.java).apply {
            if (sessionId.isNullOrBlank()) {
                log.info("Ignoring bot missing session id: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Missing session id")

            } else if (!sessionIds.values.contains(sessionId)) {
                log.info("Ignoring bot missing session id: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Invalid session id")

            } else if (botSecrets.isNotEmpty() && !botSecrets.contains(secret)) {
                log.info("Ignoring bot using invalid secret: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Invalid secret")

            } else {
                botConnections += conn
                botHandshakes[conn] = this
                listener.onBotJoined(conn, this)
            }
        }
    }

    private fun handleObserverHandshake(conn: WebSocket, message: String) {
        gson.fromJson(message, ObserverHandshake::class.java).apply {
            if (sessionId.isNullOrBlank()) {
                log.info("Ignoring observer missing session id: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Missing session id")

            } else if (!sessionIds.values.contains(sessionId)) {
                log.info("Ignoring observer missing session id: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Invalid session id")

            } else if (controllerSecrets.isNotEmpty() && !controllerSecrets.contains(secret)) {
                log.info("Ignoring observer using invalid secret: name: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Invalid secret")

            } else {
                observerConnections += conn
                observerHandshakes[conn] = this
                listener.onObserverJoined(conn, this)
            }
        }
    }

    private fun handleControllerHandshake(conn: WebSocket, message: String) {
        gson.fromJson(message, ControllerHandshake::class.java).apply {
            if (sessionId.isNullOrBlank()) {
                log.info("Ignoring controller missing session id: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Missing session id")

            } else if (!sessionIds.values.contains(sessionId)) {
                log.info("Ignoring controller missing session id: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Invalid session id")

            } else if (controllerSecrets.isNotEmpty() && !controllerSecrets.contains(secret)) {
                log.info("Ignoring controller using invalid secret: name: $name, version: $version")
                conn.close(StatusCode.POLICY_VIOLATION.value, "Invalid secret")

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

    private fun handleNextTurn() {
        executorService.submit(listener::onNextTurn)
    }

    private fun handleChangeTps(message: String) {
        executorService.submit {
            gson.fromJson(message, ChangeTps::class.java).apply {
                listener.onChangeTps(tps)
            }
        }
    }
}