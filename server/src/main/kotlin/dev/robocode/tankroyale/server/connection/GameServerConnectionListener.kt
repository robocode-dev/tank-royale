package dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.*
import dev.robocode.tankroyale.server.core.GameServer
import org.java_websocket.WebSocket
import org.slf4j.LoggerFactory

class GameServerConnectionListener(private val gameServer: GameServer) : IConnectionListener {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun onException(clientSocket: WebSocket?, exception: Exception) {
        if (clientSocket != null) {
            log.error("Bot error: client: {}, message: {}", clientSocket.remoteSocketAddress, exception.message)
        } else {
            log.error("Bot error: message: {}", exception.message)
        }
    }

    override fun onBotJoined(clientSocket: WebSocket, handshake: BotHandshake) {
        log.info("Bot joined: {}", getDisplayName(handshake.name, handshake.version))
        gameServer.handleBotJoined()
    }

    override fun onBotLeft(clientSocket: WebSocket, handshake: BotHandshake) {
        log.info("Bot left: {}", getDisplayName(handshake.name, handshake.version))
        gameServer.handleBotLeft(clientSocket)
    }

    override fun onBotReady(clientSocket: WebSocket, handshake: BotHandshake) {
        log.info("Bot ready: {}", getDisplayName(handshake.name, handshake.version))
        gameServer.handleBotReady(clientSocket)
    }

    override fun onBotIntent(clientSocket: WebSocket, handshake: BotHandshake, intent: BotIntent) {
        log.debug("Bot intent: {}: {}", getDisplayName(handshake.name, handshake.version), intent)
        gameServer.handleBotIntent(clientSocket, intent)
    }

    override fun onObserverJoined(clientSocket: WebSocket, handshake: ObserverHandshake) {
        log.info("Observer joined: {}", getDisplayName(handshake.name, handshake.version))
        gameServer.sendBotListUpdate(clientSocket)
    }

    override fun onObserverLeft(clientSocket: WebSocket, handshake: ObserverHandshake) {
        log.info("Observer left: {}", getDisplayName(handshake.name, handshake.version))
    }

    override fun onControllerJoined(clientSocket: WebSocket, handshake: ControllerHandshake) {
        log.info("Controller joined: {}", getDisplayName(handshake.name, handshake.version))
        gameServer.sendBotListUpdate(clientSocket)
    }

    override fun onControllerLeft(clientSocket: WebSocket, handshake: ControllerHandshake) {
        log.info("Controller left: {}", getDisplayName(handshake.name, handshake.version))
    }

    override fun onStartGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>, debugMode: Boolean) {
        log.debug("Game is requested to start")
        gameServer.handleStartGame(gameSetup, botAddresses, debugMode)
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

    override fun onBotPolicyUpdated(botPolicyUpdate: BotPolicyUpdate) {
        log.debug("Bot policy updated for botId {}: debugGraphics is {}", botPolicyUpdate.botId,
            if (botPolicyUpdate.debuggingEnabled) "enabled" else "disabled")
        gameServer.handleBotPolicyUpdate(botPolicyUpdate)
    }

    override fun onEnableDebugMode() {
        log.info("Debug mode is requested to be enabled")
        gameServer.handleEnableDebugMode()
    }

    override fun onDisableDebugMode() {
        log.info("Debug mode is requested to be disabled")
        gameServer.handleDisableDebugMode()
    }

    private fun getDisplayName(name: String, version: String): String =
        "${name.trim()} ${version.trim()}".trim()
}
