package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.game.*
import org.java_websocket.WebSocket

/** Connection listener interface. */
interface IConnectionListener {
    fun onException(clientSocket: WebSocket?, exception: Exception)
    fun onBotJoined(clientSocket: WebSocket, handshake: BotHandshake)
    fun onBotLeft(clientSocket: WebSocket, handshake: BotHandshake)
    fun onBotReady(clientSocket: WebSocket, handshake: BotHandshake)
    fun onBotIntent(clientSocket: WebSocket, handshake: BotHandshake, intent: BotIntent)
    fun onObserverJoined(clientSocket: WebSocket, handshake: ObserverHandshake)
    fun onObserverLeft(clientSocket: WebSocket, handshake: ObserverHandshake)
    fun onControllerJoined(clientSocket: WebSocket, handshake: ControllerHandshake)
    fun onControllerLeft(clientSocket: WebSocket, handshake: ControllerHandshake)
    fun onStartGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>)
    fun onAbortGame()
    fun onPauseGame()
    fun onResumeGame()
    fun onNextTurn()
    fun onChangeTps(tps: Int)
    fun onBotPolicyUpdated(botPolicyUpdate: BotPolicyUpdate)
}
