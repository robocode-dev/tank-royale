package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.conn

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.Server
import dev.robocode.tankroyale.server.core.ServerSetup
import dev.robocode.tankroyale.server.version.getVersion
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class ConnHandler internal constructor(
    private val setup: ServerSetup,
    private val listener: ConnListener,
    clientSecret: String?
) {
    private val clientSecret: String? = clientSecret?.trim()
    private val webSocketObserver: WebSocketObserver
    private val allConnections = Collections.synchronizedSet(HashSet<WebSocket>())
    private val botConnections = Collections.synchronizedSet(HashSet<WebSocket>())
    private val observerConnections = Collections.synchronizedSet(HashSet<WebSocket>())
    private val controllerConnections = Collections.synchronizedSet(HashSet<WebSocket>())
    private val botHandshakes = Collections.synchronizedMap(HashMap<WebSocket, BotHandshake>())
    private val observerHandshakes = Collections.synchronizedMap(HashMap<WebSocket, ObserverHandshake>())
    private val controllerHandshakes = Collections.synchronizedMap(HashMap<WebSocket, ControllerHandshake>())
    private val executorService: ExecutorService

    private val logger = LoggerFactory.getLogger(ConnHandler::class.java)

    init {
        val address = InetSocketAddress("localhost", Server.port.toInt())
        webSocketObserver = WebSocketObserver(address)
        webSocketObserver.connectionLostTimeout = 10 // TODO: Put this in a config file.
        executorService = Executors.newCachedThreadPool()
    }

    fun start() {
        webSocketObserver.run()
    }

    fun stop() {
        shutdownAndAwaitTermination(executorService)
    }

    fun broadcastToBots(message: String) {
        broadcast(getBotConnections(), message)
    }

    fun broadcastToObserverAndControllers(message: String) {
        broadcast(observerAndControllerConnections, message)
    }

    fun getBotConnections(): Set<WebSocket> {
        return botConnections.toSet()
    }

    val observerAndControllerConnections: Set<WebSocket>
        get() {
            val combined: MutableSet<WebSocket> = HashSet()
            combined += observerConnections
            combined += controllerConnections
            return combined.toSet()
        }

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> {
        return botHandshakes.toMap()
    }

    fun getBotConnections(botAddresses: Collection<BotAddress>): Set<WebSocket> {
        val foundConnections: MutableSet<WebSocket> = HashSet()
        for (conn: WebSocket in botHandshakes.keys) {
            val addr = conn.remoteSocketAddress
            if (addr != null) {
                val port = addr.port
                val hostname = addr.hostName
                for (botAddr: BotAddress in botAddresses) {
                    if (botAddr.host == hostname && botAddr.port == port) {
                        foundConnections += conn
                        break
                    }
                }
            }
        }
        return foundConnections
    }

    private fun shutdownAndAwaitTermination(pool: ExecutorService) {
        pool.shutdown() // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow()
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("Pool did not terminate")
                }
            }
        } catch (ie: InterruptedException) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow()
            // Preserve interrupt status
            Thread.currentThread().interrupt()
        }
    }

    private fun send(conn: WebSocket, message: String) {
        logger.debug("Sending to: ${conn.remoteSocketAddress}, message: $message")
        conn.send(message)
    }

    private fun broadcast(clients: Collection<WebSocket>, message: String) {
        logger.debug("Broadcast message: $message")
        webSocketObserver.broadcast(message, clients)
    }

    private fun notifyException(exception: Exception) {
        logger.debug("Exception occurred: $exception")
        executorService.submit { listener.onException(exception) }
    }

    private inner class WebSocketObserver(address: InetSocketAddress) : WebSocketServer(address) {

        override fun onStart() {}

        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            logger.debug("onOpen(): ${conn.remoteSocketAddress}")
            allConnections += conn
            val hs = ServerHandshake()
            hs.`$type` = Message.`$type`.SERVER_HANDSHAKE
            hs.variant = "Tank Royale" // Robocode Tank Royale
            hs.version = getVersion()
            hs.gameTypes = setup.gameTypes
            val msg = Gson().toJson(hs)
            send(conn, msg)
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            logger.debug("onClose(): ${conn.remoteSocketAddress}, code: $code, reason: $reason, remote: $remote")
            allConnections -= conn
            when {
                botConnections.remove(conn) -> {
                    val handshake = botHandshakes[conn]
                    executorService.submit { listener.onBotLeft(conn, handshake!!) }
                    botHandshakes -= conn
                }
                observerConnections.remove(conn) -> {
                    val handshake = observerHandshakes[conn]
                    executorService.submit { listener.onObserverLeft(conn, handshake!!) }
                    observerHandshakes -= conn
                }
                controllerConnections.remove(conn) -> {
                    val handshake = controllerHandshakes[conn]
                    executorService.submit { listener.onControllerLeft(conn, handshake!!) }
                    controllerHandshakes -= conn
                }
            }
        }

        override fun onMessage(conn: WebSocket, message: String) {
            logger.debug("onMessage(): ${conn.remoteSocketAddress}, message: $message")
            val gson = Gson()
            try {
                val jsonObject = gson.fromJson(message, JsonObject::class.java)
                val jsonType = jsonObject["\$type"]
                if (jsonType != null) {
                    val type: Message.`$type`
                    try {
                        type = Message.`$type`.fromValue(jsonType.asString)
                    } catch (ex: IllegalArgumentException) {
                        notifyException(IllegalStateException("Unhandled message type: ${jsonType.asString}"))
                        return
                    }
                    logger.debug("Handling message: $type")
                    when (type) {
                        Message.`$type`.BOT_HANDSHAKE -> {
                            val handshake = gson.fromJson(message, BotHandshake::class.java)
                            botConnections += conn
                            botHandshakes[conn] = handshake
                            executorService.submit { listener.onBotJoined(conn, handshake) }
                        }
                        Message.`$type`.OBSERVER_HANDSHAKE -> {
                            val handshake = gson.fromJson(
                                message,
                                ObserverHandshake::class.java
                            )

                            // Validate client secret before continuing
                            if (clientSecret != null && clientSecret.isNotEmpty() && handshake.secret != clientSecret) {
                                logger.info("Ignoring observer using invalid secret. Name: ${handshake.name}, Version: ${handshake.version}")
                                return  // Ignore client with wrong secret
                            }
                            observerConnections += conn
                            observerHandshakes[conn] = handshake
                            executorService.submit { listener.onObserverJoined(conn, handshake) }
                        }
                        Message.`$type`.CONTROLLER_HANDSHAKE -> {
                            val handshake = gson.fromJson(message, ControllerHandshake::class.java)

                            // Validate client secret before continuing
                            if (clientSecret != null && clientSecret.isNotEmpty() && handshake.secret != clientSecret) {
                                logger.info("Ignoring controller using invalid secret. Name: ${handshake.name}, Version: ${handshake.version}")
                                return  // Ignore client with wrong secret
                            }
                            controllerConnections += conn
                            controllerHandshakes[conn] = handshake
                            executorService.submit { listener.onControllerJoined(conn, handshake) }
                        }
                        Message.`$type`.BOT_READY -> {
                            executorService.submit { listener.onBotReady(conn) }
                        }
                        Message.`$type`.BOT_INTENT -> {
                            val intent = gson.fromJson(message, BotIntent::class.java)
                            executorService.submit { listener.onBotIntent(conn, intent) }
                        }
                        Message.`$type`.START_GAME -> {
                            val startGame = gson.fromJson(message, StartGame::class.java)
                            val gameSetup = startGame.gameSetup
                            val botAddresses: Collection<BotAddress> = startGame.botAddresses
                            executorService.submit { listener.onStartGame(gameSetup, botAddresses) }
                        }
                        Message.`$type`.STOP_GAME -> {
                            executorService.submit(listener::onAbortGame)
                        }
                        Message.`$type`.PAUSE_GAME -> {
                            executorService.submit(listener::onPauseGame)
                        }
                        Message.`$type`.RESUME_GAME -> {
                            executorService.submit(listener::onResumeGame)
                        }
                        Message.`$type`.CHANGE_TPS -> {
                            val changeTps = gson.fromJson(message, ChangeTps::class.java)
                            executorService.submit { listener.onChangeTps(changeTps.tps) }
                        }
                        else -> notifyException(IllegalStateException("Unhandled message type: $type"))
                    }
                }
            } catch (e2: JsonSyntaxException) {
                logger.error("Invalid message: $message", e2)
            }
        }

        override fun onError(conn: WebSocket, ex: Exception) {
            notifyException(ex)
        }
    }
}