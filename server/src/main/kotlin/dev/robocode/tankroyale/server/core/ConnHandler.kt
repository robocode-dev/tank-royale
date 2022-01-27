package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core

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
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConnHandler internal constructor(
    private val setup: ServerSetup,
    private val listener: ConnListener,
    clientSecret: String?
) {
    private val clientSecret: String? = clientSecret?.trim()
    private val webSocketObserver: WebSocketObserver
    private val allConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val botConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val observerConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val controllerConnections = ConcurrentHashMap.newKeySet<WebSocket>()
    private val botHandshakes = ConcurrentHashMap<WebSocket, BotHandshake>()
    private val observerHandshakes = ConcurrentHashMap<WebSocket, ObserverHandshake>()
    private val controllerHandshakes = ConcurrentHashMap<WebSocket, ControllerHandshake>()
    private val executorService: ExecutorService

    private val log = LoggerFactory.getLogger(ConnHandler::class.java)

    init {
        val address = InetSocketAddress("localhost", Server.port.toInt())
        webSocketObserver = WebSocketObserver(address)
        webSocketObserver.isTcpNoDelay = true
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

    fun getBotConnections(): Set<WebSocket> {
        return botConnections.toSet()
    }

    val observerAndControllerConnections: Set<WebSocket>
        get() {
            val combined = mutableSetOf<WebSocket>()
            combined += observerConnections
            combined += controllerConnections
            return combined.toSet()
        }

    fun getBotHandshakes(): Map<WebSocket, BotHandshake> {
        return botHandshakes.toMap()
    }

    fun getBotConnections(botAddresses: Collection<BotAddress>): Set<WebSocket> {
        val foundConnections = mutableSetOf<WebSocket>()
        for (conn: WebSocket in botHandshakes.keys) {
            val address = conn.remoteSocketAddress
            if (address != null) {
                val port = address.port
                val hostname = toIpAddress(address.hostName)
                for (botAddr: BotAddress in botAddresses) {
                    if (toIpAddress(botAddr.host) == hostname && botAddr.port == port) {
                        foundConnections += conn
                        break
                    }
                }
            }
        }
        return foundConnections
    }

    private fun toIpAddress(host: String): String =
        if (host.lowercase(Locale.getDefault()) == "localhost") "127.0.0.1" else host

    private fun shutdownAndAwaitTermination(pool: ExecutorService) {
        pool.shutdown() // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                pool.shutdownNow()
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Pool did not terminate")
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
                val hs = ServerHandshake()
                hs.`$type` = Message.`$type`.SERVER_HANDSHAKE
                hs.variant = "Tank Royale" // Robocode Tank Royale
                hs.version = getVersion() ?: "?"
                hs.gameTypes = setup.gameTypes
                val msg = Gson().toJson(hs)
                send(conn, msg)
            }
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            log.debug("onClose(): ${conn.remoteSocketAddress}, code: $code, reason: $reason, remote: $remote")

            executorService.submit {
                allConnections -= conn
                when {
                    botConnections.remove(conn) -> {
                        val handshake = botHandshakes[conn]
                        listener.onBotLeft(conn, handshake!!)
                        botHandshakes -= conn
                    }
                    observerConnections.remove(conn) -> {
                        val handshake = observerHandshakes[conn]
                        listener.onObserverLeft(conn, handshake!!)
                        observerHandshakes -= conn
                    }
                    controllerConnections.remove(conn) -> {
                        val handshake = controllerHandshakes[conn]
                        listener.onControllerLeft(conn, handshake!!)
                        controllerHandshakes -= conn
                    }
                }
            }
        }

        override fun onMessage(conn: WebSocket, message: String) {
            log.debug("onMessage(): ${conn.remoteSocketAddress}, message: $message")

            executorService.submit {
                val gson = Gson()
                try {
                    val jsonObject = gson.fromJson(message, JsonObject::class.java)
                    val jsonType = jsonObject["\$type"]
                    if (jsonType != null) {
                        val type: Message.`$type`
                        try {
                            type = Message.`$type`.fromValue(jsonType.asString)

                            log.debug("Handling message: $type")
                            when (type) {
                                Message.`$type`.BOT_INTENT -> {
                                    val intent = gson.fromJson(message, BotIntent::class.java)
                                    listener.onBotIntent(conn, botHandshakes[conn]!!, intent)
                                }
                                Message.`$type`.BOT_HANDSHAKE -> {
                                    val handshake = gson.fromJson(message, BotHandshake::class.java)
                                    botConnections += conn
                                    botHandshakes[conn] = handshake
                                    listener.onBotJoined(conn, handshake)
                                }
                                Message.`$type`.OBSERVER_HANDSHAKE -> {
                                    val handshake = gson.fromJson(
                                        message,
                                        ObserverHandshake::class.java
                                    )
                                    // Validate client secret before continuing
                                    var validClient = true
                                    if (clientSecret != null && clientSecret.isNotEmpty() && handshake.secret != clientSecret) {
                                        log.info("Ignoring observer using invalid secret. Name: ${handshake.name}, Version: ${handshake.version}")
                                        validClient = false // Ignore client with wrong secret
                                    }
                                    if (validClient) {
                                        observerConnections += conn
                                        observerHandshakes[conn] = handshake
                                        listener.onObserverJoined(conn, handshake)
                                    }
                                }
                                Message.`$type`.CONTROLLER_HANDSHAKE -> {
                                    val handshake = gson.fromJson(message, ControllerHandshake::class.java)

                                    // Validate client secret before continuing
                                    var validClient = true
                                    if (clientSecret != null && clientSecret.isNotEmpty() && handshake.secret != clientSecret) {
                                        log.info("Ignoring controller using invalid secret. Name: ${handshake.name}, Version: ${handshake.version}")
                                        validClient = false // Ignore client with wrong secret
                                    }
                                    if (validClient) {
                                        controllerConnections += conn
                                        controllerHandshakes[conn] = handshake
                                        listener.onControllerJoined(conn, handshake)
                                    }
                                }
                                Message.`$type`.BOT_READY -> {
                                    listener.onBotReady(conn, botHandshakes[conn]!!)
                                }
                                Message.`$type`.START_GAME -> {
                                    val startGame = gson.fromJson(message, StartGame::class.java)
                                    listener.onStartGame(startGame.gameSetup, startGame.botAddresses)
                                }
                                Message.`$type`.STOP_GAME -> executorService.submit(listener::onAbortGame)
                                Message.`$type`.PAUSE_GAME -> executorService.submit(listener::onPauseGame)
                                Message.`$type`.RESUME_GAME -> executorService.submit(listener::onResumeGame)
                                Message.`$type`.CHANGE_TPS -> executorService.submit {
                                    val changeTps = gson.fromJson(message, ChangeTps::class.java)
                                    listener.onChangeTps(changeTps.tps)
                                }
                                else -> notifyException(IllegalStateException("Unhandled message type: $type"))
                            }
                        } catch (iae: IllegalArgumentException) {
                            notifyException(IllegalStateException("Unhandled message type: ${jsonType.asString}"))
                        }
                    }
                } catch (jse: JsonSyntaxException) {
                    log.error("Invalid message: $message", jse)
                } catch (e: Exception) {
                    log.error("Error when passing message: $message", e)
                }
            }
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            // Do noting
        }
    }
}