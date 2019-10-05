package dev.robocode.tankroyale.ui.desktop.ui.battle

import kotlinx.serialization.ImplicitReflectionSerializer
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onActivated
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onDeactivated
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.MainWindow
import dev.robocode.tankroyale.ui.desktop.ui.ResourceBundles
import java.awt.Dimension
import java.awt.EventQueue
import java.io.Closeable
import javax.swing.*
import javax.swing.JOptionPane.YES_OPTION

@ImplicitReflectionSerializer
object BattleDialog : JDialog(MainWindow, getWindowTitle()) {

    private val tabbedPane = JTabbedPane()
    private val setupRulesPanel = SetupRulesPanel()
    private var onErrorCloseable: Closeable? = null

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 450)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(tabbedPane)
        tabbedPane.addTab(ResourceBundles.UI_TITLES.get("select_bots_tab"), SelectBotsPanel)
        tabbedPane.addTab(ResourceBundles.UI_TITLES.get("setup_rules_tab"), setupRulesPanel)

        tabbedPane.selectedComponent = setupRulesPanel

        onActivated {
            startServerOrCloseDialog()
        }

        onDeactivated {
            onErrorCloseable?.close()
            onErrorCloseable = null
        }
    }

    fun selectBotsTab() {
        tabbedPane.selectedComponent = SelectBotsPanel
    }

    fun selectSetupRulesTab() {
        tabbedPane.selectedComponent = setupRulesPanel
    }

    private fun startServerOrCloseDialog() {
        // If no server is running => start server
        if (!ServerProcess.isRunning()) {
            ServerProcess.start()
        }

        var url = ServerSettings.defaultUrl

        // Error handler that shows a dialog asking the user to start a local server or dismiss battle dialog
        onErrorCloseable = Client.onError.subscribe {
            val option = JOptionPane.showConfirmDialog(
                this,
                String.format(ResourceBundles.MESSAGES.get("no_connection__start_server_question"), url),
                ResourceBundles.UI_TITLES.get("question"),
                JOptionPane.YES_NO_OPTION
            )
            if (option == YES_OPTION) {
                ServerProcess.start()
                Client.connect(url)
            } else {
                dispose() // dispose the dialog, when server is not available
            }
        }

        if (ServerProcess.isRunning() && ServerSettings.port != ServerProcess.port) {
            Client.close()
            ServerProcess.stop()
            ServerProcess.start()
        }

        // Connect to the server. The error handler above is triggered if the connection cannot be established
        if (!Client.isConnected) {
            Client.connect(ServerSettings.defaultUrl)
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("battle_dialog")
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BattleDialog.isVisible = true
    }
}