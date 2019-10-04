package dev.robocode.tankroyale.ui.desktop.ui.server

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import dev.robocode.tankroyale.ui.desktop.util.Event
import dev.robocode.tankroyale.ui.desktop.util.ICommand
import java.io.Closeable
import javax.swing.JOptionPane

class TestServerConnectionCommand(private val serverUrl: String) : ICommand {

    val onCompleted = Event<Unit>()

    private val disposables = ArrayList<Closeable>()

    override fun execute() {
        dispose()

        // Cleanup when connected, disconnected or error occurs
        disposables += Client.onConnected.subscribe { complete() }
        disposables += Client.onDisconnected.subscribe { complete() }
        disposables += Client.onError.subscribe { complete() }

        val wasAlreadyConnected = Client.isConnected

        disposables += Client.onConnected.subscribe {
            JOptionPane.showMessageDialog(
                null, ResourceBundles.MESSAGES.get("connected_successfully_to_server")
            )
            // Close server, if it was closed prior to testing
            if (!wasAlreadyConnected) {
                Client.close()
            }
        }

        ConnectToServerCommand(serverUrl).execute()
    }

    private fun complete() {
        onCompleted.publish(Unit)
        dispose()
    }

    private fun dispose() {
        disposables.forEach { it.close() }
    }
}
