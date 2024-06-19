package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.core.GameServer
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory

class GameServerConnectionListener(private val gameServer: GameServer) : IConnectionListener {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun onException(clientConnection: WebSocket, exception: Exception) {
        log.error("Bot error: client: {}, message: {}", clientConnection.remoteSocketAddress, exception.message)
        exception.printStackTrace()
    }

    override fun onBotJoined(clientConnection: WebSocket, handshake: BotHandshake) {
        log.info("Bot joined: {}", getDisplayName(handshake))
        gameServer.handleBotJoined()
    }

    override fun onBotLeft(clientConnection: WebSocket, handshake: BotHandshake) {
        log.info("Bot left: {}", getDisplayName(handshake))
        gameServer.handleBotLeft(clientConnection)
    }

    override fun onBotReady(clientConnection: WebSocket, handshake: BotHandshake) {
        log.info("Bot ready: {}", getDisplayName(handshake))
        gameServer.handleBotReady(clientConnection)
    }

    override fun onBotIntent(clientConnection: WebSocket, handshake: BotHandshake, intent: BotIntent) {
        log.info("Bot ready: {}, intent: {}", getDisplayName(handshake), intent)
        gameServer.handleBotIntent(clientConnection, intent)
    }

    override fun onObserverJoined(clientConnection: WebSocket, handshake: ObserverHandshake) {
        log.info("Observer joined: {}", getDisplayName(handshake))
        gameServer.sendBotListUpdate(clientConnection)
    }

    override fun onObserverLeft(clientConnection: WebSocket, handshake: ObserverHandshake) {
        log.info("Observer left: {}", getDisplayName(handshake))
    }

    override fun onControllerJoined(clientConnection: WebSocket, handshake: ControllerHandshake) {
        log.info("Controller joined: {}", getDisplayName(handshake))
        gameServer.sendBotListUpdate(clientConnection)
    }

    override fun onControllerLeft(clientConnection: WebSocket, handshake: ControllerHandshake) {
        log.info("Controller left: {}", getDisplayName(handshake))
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
        log.info("TPS is requested to change to {}", tps)
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
