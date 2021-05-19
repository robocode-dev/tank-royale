package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.ui.ResourceBundles.MESSAGES
import dev.robocode.tankroyale.gui.ui.ResourceBundles.UI_TITLES
import dev.robocode.tankroyale.gui.util.ICommand
import dev.robocode.tankroyale.gui.ui.new_server.WsUrl
import java.awt.EventQueue
import java.io.Closeable
import java.net.ConnectException
import java.net.UnknownHostException
import javax.swing.JOptionPane

class ConnectToServerCommand(private val serverUrl: String) : ICommand {

    private val disposables = ArrayList<Closeable>()

    override fun execute() {
        dispose()

        // Cleanup when connected, disconnected or error occurs
        disposables += Client.onConnected.subscribe { dispose() }
        disposables += Client.onDisconnected.subscribe { dispose() }
        disposables += Client.onError.subscribe { dispose() }

        // Handle case connection cannot be established with the server
        disposables += Client.onError.subscribe { exception ->
            if (exception is ConnectException || exception is UnknownHostException) {
                val option = JOptionPane.showConfirmDialog(
                    null,
                    String.format(MESSAGES.get("no_connection__start_server_question"), serverUrl),
                    UI_TITLES.get("question"),
                    JOptionPane.YES_NO_OPTION
                )
                if (option == JOptionPane.YES_OPTION) {
                    EventQueue.invokeLater {
                        StartServerDialog.isVisible = true
                    }
                }
            }
        }

        // (Re)connect to the server
        Client.connect(WsUrl(serverUrl).origin)
    }

    private fun dispose() {
        disposables.forEach { it.close() }
    }
}