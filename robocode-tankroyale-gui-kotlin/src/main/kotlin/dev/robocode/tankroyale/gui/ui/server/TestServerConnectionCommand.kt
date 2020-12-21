package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.ICommand
import java.io.Closeable

class TestServerConnectionCommand(private val serverUrl: String) : ICommand {

    val onFound = Event<Unit>()
    val onNotFound = Event<Unit>()

    private val disposables = ArrayList<Closeable>()

    override fun execute() {
        dispose()

        // Cleanup when connected, disconnected or error occurs
        disposables += Client.onConnected.subscribe { publishFound() }
        disposables += Client.onDisconnected.subscribe { dispose() }
        disposables += Client.onError.subscribe { publishNotFound() }

        ConnectToServerCommand(serverUrl).execute()
    }

    private fun publishFound() {
        onFound.publish(Unit)
        dispose()
    }

    private fun publishNotFound() {
        onNotFound.publish(Unit)
        dispose()
    }

    private fun dispose() {
        disposables.forEach { it.close() }
    }
}
