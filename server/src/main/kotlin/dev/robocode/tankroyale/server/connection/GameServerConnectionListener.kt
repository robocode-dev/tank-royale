package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.core.GameServer
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory

class GameServerConnectionListener(private val gameServer: GameServer) : IConnectionListener {

    private val log = LoggerFactory.getLogger(GameServerConnectionListener::class.java)

    override fun onException(exception: Exception) {
        exception.printStackTrace()
    }

    override fun onBotJoined(conn: WebSocket, handshake: BotHandshake) {
        log.info("Bot joined: ${getDisplayName(handshake)}")
        gameServer.handleBotJoined()
    }

    override fun onBotLeft(conn: WebSocket, handshake: BotHandshake) {
        log.info("Bot left: ${getDisplayName(handshake)}")
        gameServer.handleBotLeft(conn)
    }

    override fun onBotReady(conn: WebSocket, handshake: BotHandshake) {
        log.debug("Bot ready: ${getDisplayName(handshake)}")
        gameServer.handleBotReady(conn)
    }

    override fun onBotIntent(conn: WebSocket, handshake: BotHandshake, intent: BotIntent) {
        log.debug("Bot intent: ${getDisplayName(handshake)}: $intent")
        gameServer.handleBotIntent(conn, intent)
    }

    override fun onObserverJoined(conn: WebSocket, handshake: ObserverHandshake) {
        log.info("Observer joined: ${getDisplayName(handshake)}")
        gameServer.sendBotListUpdate(conn)
    }

    override fun onObserverLeft(conn: WebSocket, handshake: ObserverHandshake) {
        log.info("Observer left: ${getDisplayName(handshake)}")
    }

    override fun onControllerJoined(conn: WebSocket, handshake: ControllerHandshake) {
        log.info("Controller joined: ${getDisplayName(handshake)}")
        gameServer.sendBotListUpdate(conn)
    }

    override fun onControllerLeft(conn: WebSocket, handshake: ControllerHandshake) {
        log.info("Controller left: ${getDisplayName(handshake)}")
    }

    override fun onStartGame(gameSetup: GameSetup, botAddresses: Collection<BotAddress>) {
        log.debug("Game is requested to start")
        gameServer.handleStartGame(gameSetup, botAddresses)
    }

    override fun onAbortGame() {
        log.debug("Game is requested to abort")
        gameServer.handleAbortGame()
    }

    override fun onPauseGame() {
        log.debug("Game is requested to pause")
        gameServer.handlePauseGame()
    }

    override fun onResumeGame() {
        log.debug("Game is requested to resume")
        gameServer.handleResumeGame()
    }

    override fun onNextTurn() {
        log.debug("Game is requested to do next turn")
        gameServer.handleNextTurn()
    }

    override fun onChangeTps(tps: Int) {
        log.info("TPS is requested to change to $tps")
        gameServer.handleChangeTps(tps)
    }

    private fun getDisplayName(handshake: BotHandshake): String =
        getDisplayName(handshake.name, handshake.version)

    private fun getDisplayName(handshake: ObserverHandshake): String =
        getDisplayName(handshake.name, handshake.version)

    private fun getDisplayName(handshake: ControllerHandshake): String =
        getDisplayName(handshake.name, handshake.version)

    private fun getDisplayName(name: String, version: String): String {
        var displayName = ""
        name.trim().apply {
            if (isNotEmpty()) {
                displayName = this
            }
        }
        version.trim().apply {
            if (isNotEmpty()) {
                displayName += " $this"
            }
        }
        return displayName
    }
}
