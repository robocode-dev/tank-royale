package dev.robocode.tankroyale.server.connection

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import dev.robocode.tankroyale.schema.game.*
import dev.robocode.tankroyale.server.core.ServerSetup
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection.IClientWebSocketObserver
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection.IConnectionListener
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core.StatusCode
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.util.VersionFileProvider
import org.java_websocket.WebSocket
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ClientHandshake
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class ClientWebSocketsHandler(
    private val setup: ServerSetup,
    private val listener: IConnectionListener,
    private val controllerSecrets: Set<String>,
    private val botSecrets: Set<String>,
    private val broadcastFunction: (clientSockets: Collection<WebSocket>, message: String) -> Unit
) : IClientWebSocketObserver, Closeable {

    companion object {
        private const val MISSING_SESSION_ID = "Missing session id"
        private const val INVALID_SECRET = "Invalid secret"
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val allSockets = ConcurrentHashMap.newKeySet<WebSocket>()

    private val botSockets = ConcurrentHashMap.newKeySet<WebSocket>()
    private val observerSockets = ConcurrentHashMap.newKeySet<WebSocket>()
    private val controllerSockets = ConcurrentHashMap.newKeySet<WebSocket>()

    private val sessionIds = ConcurrentHashMap<WebSocket, String /* sessionId */>()

    private val botHandshakes = ConcurrentHashMap<WebSocket, BotHandshake>()
    private val observerHandshakes = ConcurrentHashMap<WebSocket, ObserverHandshake>()
    private val controllerHandshakes = ConcurrentHashMap<WebSocket, ControllerHandshake>()

    private val executorService = Executors.newCachedThreadPool()

    private val gson = Gson()

    private var currentGameSetup: GameSetup? = null

    override fun close() {
        shutdownAndAwaitTermination(executorService)
    }

    override fun onOpen(clientSocket: WebSocket, handshake: ClientHandshake) {
        addSocketAndSendServerHandshake(clientSocket)
    }

    override fun onClose(clientSocket: WebSocket, code: Int, reason: String, remote: Boolean) {
        removeSocket(clientSocket)
    }

    override fun onMessage(clientSocket: WebSocket, message: String) {
        processMessage(clientSocket, message)
    }

    override fun onError(clientSocket: WebSocket?, exception: Exception) {
        handleException(clientSocket, exception)
    }

    private fun addSocketAndSendServerHandshake(clientSocket: WebSocket) {
        allSockets += clientSocket

        ServerHandshake().apply {
            type = Message.Type.SERVER_HANDSHAKE
            name = "Robocode Tank Royale server"
            sessionId = generateAndStoreSessionId(clientSocket)
            variant = "Tank Royale"
            version = VersionFileProvider.version
            gameTypes = setup.gameTypes
            gameSetup = currentGameSetup
        }.also {
            send(clientSocket, Gson().toJson(it))
        }
    }

    private fun removeSocket(clientSocket: WebSocket) {
        closeSocket(clientSocket)
    }

    private fun processMessage(clientSocket: WebSocket, message: String) {
        executorService.submit {
            try {
                gson.fromJson(message, JsonObject::class.java)["type"]?.let { jsonType ->
                    try {
                        val type = Message.Type.fromValue(jsonType.asString)

                        log.debug("Handling message: {}", type)
                        when (type) {
                            Message.Type.BOT_INTENT -> handleIntent(clientSocket, message)
                            Message.Type.BOT_HANDSHAKE -> handleBotHandshake(clientSocket, message)
                            Message.Type.OBSERVER_HANDSHAKE -> handleObserverHandshake(clientSocket, message)
                            Message.Type.CONTROLLER_HANDSHAKE -> handleControllerHandshake(clientSocket, message)
                            Message.Type.BOT_READY -> handleBotReady(clientSocket)
                            Message.Type.START_GAME -> handleStartGame(message)
                            Message.Type.STOP_GAME -> handleStopGame()
                            Message.Type.PAUSE_GAME -> handlePauseGame()
                            Message.Type.RESUME_GAME -> handleResumeGame()
                            Message.Type.NEXT_TURN -> handleNextTurn()
                            Message.Type.CHANGE_TPS -> handleChangeTps(message)
                            Message.Type.BOT_POLICY_UPDATE -> handleBotPolicyUpdated(message)
                            else -> handleException(
                                clientSocket,
                                IllegalStateException("Unhandled message type: $type")
                            )
                        }
                    } catch (ex: IllegalArgumentException) {
                        handleException(
                            clientSocket,
                            IllegalStateException("Unhandled message type: ${jsonType.asString}")
                        )
                    }
                }
            } catch (exception: JsonSyntaxException) {
                log.error("Invalid message: $message", exception)
            } catch (exception: Exception) {
                log.error("Error when passing message: $message", exception)
            }
        }
    }

    fun getBotSockets(): Set<WebSocket> = botSockets.toSet()

    fun getObserverAndControllerSockets(): Set<WebSocket> = observerSockets.union(controllerSockets)

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> = botHandshakes

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

    override fun send(clientSocket: WebSocket, message: String) {
        log.debug("Send to: client: {}, message: {}", clientSocket.remoteSocketAddress, message)

        executorService.submit {
            try {
                clientSocket.send(message)
            } catch (e: WebsocketNotConnectedException) {
                closeSocket(clientSocket)
            }
        }
    }

    override fun broadcast(clientSockets: Collection<WebSocket>, message: String) {
        log.debug("Broadcast to clients: message: {}", message)

        executorService.submit {
            broadcastFunction(clientSockets, message)
        }
    }

    private fun closeSocket(clientSocket: WebSocket) {
        allSockets -= clientSocket
        when {
            botSockets.remove(clientSocket) -> handleBotLeft(clientSocket)
            observerSockets.remove(clientSocket) -> handleObserverLeft(clientSocket)
            controllerSockets.remove(clientSocket) -> handleControllerLeft(clientSocket)
        }
        sessionIds.remove(clientSocket)
    }

    private fun handleBotLeft(clientSocket: WebSocket) {
        botHandshakes[clientSocket]?.let {
            listener.onBotLeft(clientSocket, it)
        }
        botHandshakes -= clientSocket
    }

    private fun handleObserverLeft(clientSocket: WebSocket) {
        observerHandshakes[clientSocket]?.let {
            listener.onObserverLeft(clientSocket, it)
        }
        observerHandshakes -= clientSocket
    }

    private fun handleControllerLeft(clientSocket: WebSocket) {
        controllerHandshakes[clientSocket]?.let {
            listener.onControllerLeft(clientSocket, it)
        }
        controllerHandshakes -= clientSocket
    }

    private fun generateAndStoreSessionId(clientSocket: WebSocket): String {
        val sessionId = generateSessionId()
        check(!sessionIds.values.contains(sessionId)) {
            "Generated session id has been generated before. It must be unique"
        }
        sessionIds[clientSocket] = sessionId
        return sessionId
    }

    private fun generateSessionId(): String {
        val uuid = UUID.randomUUID()
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(uuid.mostSignificantBits)
        byteBuffer.putLong(uuid.leastSignificantBits)
        return Base64.getEncoder().withoutPadding().encodeToString(byteBuffer.array())
    }

    private fun handleIntent(clientSocket: WebSocket, message: String) {
        botHandshakes[clientSocket]?.let { botHandshake ->
            val intent = gson.fromJson(message, BotIntent::class.java)
            listener.onBotIntent(clientSocket, botHandshake, intent)
        }
    }

    private fun handleBotHandshake(clientSocket: WebSocket, message: String) {
        gson.fromJson(message, BotHandshake::class.java).apply {
            if (sessionId.isNullOrBlank() || !sessionIds.values.contains(sessionId)) {
                log.info("Ignoring bot missing session id: $name, version: $version")
                clientSocket.close(StatusCode.POLICY_VIOLATION.value, MISSING_SESSION_ID)

            } else if (botSecrets.isNotEmpty() && !botSecrets.contains(secret)) {
                log.info("Ignoring bot using invalid secret: $name, version: $version")
                clientSocket.close(StatusCode.POLICY_VIOLATION.value, INVALID_SECRET)

            } else {
                botSockets += clientSocket
                botHandshakes[clientSocket] = this
                listener.onBotJoined(clientSocket, this)
            }
        }
    }

    private fun handleObserverHandshake(clientSocket: WebSocket, message: String) {
        gson.fromJson(message, ObserverHandshake::class.java).apply {
            if (sessionId.isNullOrBlank() || !sessionIds.values.contains(sessionId)) {
                log.info("Ignoring observer missing session id: $name, version: $version")
                clientSocket.close(StatusCode.POLICY_VIOLATION.value, MISSING_SESSION_ID)

            } else if (controllerSecrets.isNotEmpty() && !controllerSecrets.contains(secret)) {
                log.info("Ignoring observer using invalid secret: name: $name, version: $version")
                clientSocket.close(StatusCode.POLICY_VIOLATION.value, INVALID_SECRET)

            } else {
                observerSockets += clientSocket
                observerHandshakes[clientSocket] = this
                listener.onObserverJoined(clientSocket, this)
            }
        }
    }

    private fun handleControllerHandshake(clientSocket: WebSocket, message: String) {
        gson.fromJson(message, ControllerHandshake::class.java).apply {
            if (sessionId.isNullOrBlank() || !sessionIds.values.contains(sessionId)) {
                log.info("Ignoring controller missing session id: $name, version: $version")
                clientSocket.close(StatusCode.POLICY_VIOLATION.value, MISSING_SESSION_ID)

            } else if (controllerSecrets.isNotEmpty() && !controllerSecrets.contains(secret)) {
                log.info("Ignoring controller using invalid secret: name: $name, version: $version")
                clientSocket.close(StatusCode.POLICY_VIOLATION.value, INVALID_SECRET)

            } else {
                controllerSockets += clientSocket
                controllerHandshakes[clientSocket] = this
                listener.onControllerJoined(clientSocket, this)
            }
        }
    }

    private fun handleBotReady(clientSocket: WebSocket) {
        botHandshakes[clientSocket]?.let { botHandshake ->
            listener.onBotReady(clientSocket, botHandshake)
        }
    }

    private fun handleStartGame(message: String) {
        gson.fromJson(message, StartGame::class.java).apply {
            currentGameSetup = gameSetup
            listener.onStartGame(gameSetup, botAddresses.toSet())
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

    private fun handleBotPolicyUpdated(message: String) {
        executorService.submit {
            gson.fromJson(message, BotPolicyUpdate::class.java).apply {
                listener.onBotPolicyUpdated(this)
            }
        }
    }

    private fun handleException(clientSocket: WebSocket?, exception: Exception) {
        listener.onException(clientSocket, exception)
        exitProcess(1) // general error
    }
}
