package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.connection

import dev.robocode.tankroyale.schema.*
import org.java_websocket.WebSocket

/** Connection listener interface. */
interface IConnectionListener {
    fun onException(clientConnection: WebSocket, exception: Exception)
    fun onBotJoined(clientConnection: WebSocket, handshake: BotHandshake)
    fun onBotLeft(clientConnection: WebSocket, handshake: BotHandshake)
    fun onBotReady(clientConnection: WebSocket, handshake: BotHandshake)
    fun onBotIntent(clientConnection: WebSocket, handshake: BotHandshake, intent: BotIntent)
    fun onObserverJoined(clientConnection: WebSocket, handshake: ObserverHandshake)
    fun onObserverLeft(clientConnection: WebSocket, handshake: ObserverHandshake)
    fun onControllerJoined(clientConnection: WebSocket, handshake: ControllerHandshake)
    fun onControllerLeft(clientConnection: WebSocket, handshake: ControllerHandshake)
    fun onStartGame(gameSetup: GameSetup, botAddresses: Set<BotAddress>)
    fun onAbortGame()
    fun onPauseGame()
    fun onResumeGame()
    fun onNextTurn()
    fun onChangeTps(tps: Int)
}
