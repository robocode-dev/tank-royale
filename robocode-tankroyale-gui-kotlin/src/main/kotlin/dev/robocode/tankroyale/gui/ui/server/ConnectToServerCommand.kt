package dev.robocode.tankroyale.gui.ui.server

import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.ui.ResourceBundles.MESSAGES
import dev.robocode.tankroyale.gui.ui.ResourceBundles.UI_TITLES
import dev.robocode.tankroyale.gui.ui.new_server.WsUrl
import dev.robocode.tankroyale.gui.util.ICommand
import java.awt.EventQueue
import java.net.ConnectException
import java.net.UnknownHostException
import javax.swing.JOptionPane

class ConnectToServerCommand(private val serverUrl: String) : ICommand {

    override fun execute() {
        // Cleanup when connected, disconnected or error occurs
        Client.apply {
            onError.subscribe(this) { exception -> handleConnectionError(exception) }

            // (Re)connect to the server
            connect(WsUrl(serverUrl).origin)
        }
    }

    private fun handleConnectionError(exception: Throwable) {
        // Handle case connection cannot be established with the server
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
}