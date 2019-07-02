package net.robocode2.gui.ui.battle

import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.WindowExt.onActivated
import net.robocode2.gui.extensions.WindowExt.onDeactivated
import net.robocode2.gui.server.ServerProcess
import net.robocode2.gui.settings.ServerSettings
import net.robocode2.gui.settings.ServerSettings.endpoint
import net.robocode2.gui.ui.MainWindow
import net.robocode2.gui.ui.ResourceBundles
import net.robocode2.gui.utils.Disposable
import java.awt.Dimension
import java.awt.EventQueue
import java.net.URI
import javax.swing.*
import javax.swing.JOptionPane.YES_OPTION

object BattleDialog : JDialog(MainWindow, getWindowTitle()) {

    private val tabbedPane = JTabbedPane()
    private val selectBotsPanel = SelectBotsPanel()
    private val setupRulesPanel = SetupRulesPanel()
    private var onErrorDisposable: Disposable? = null

    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE

        size = Dimension(600, 450)

        setLocationRelativeTo(null) // center on screen

        contentPane.add(tabbedPane)
        tabbedPane.addTab(ResourceBundles.UI_TITLES.get("select_bots_tab"), selectBotsPanel)
        tabbedPane.addTab(ResourceBundles.UI_TITLES.get("setup_rules_tab"), setupRulesPanel)

        tabbedPane.selectedComponent = setupRulesPanel

        onActivated {
            startServerOrCloseDialog()
        }

        onDeactivated {
            onErrorDisposable?.dispose()
            onErrorDisposable = null
        }
    }

    fun selectBotsTab() {
        tabbedPane.selectedComponent = selectBotsPanel
    }

    fun selectSetupRulesTab() {
        tabbedPane.selectedComponent = setupRulesPanel
    }

    private fun startServerOrCloseDialog() {
        // If no server is running and a local server must be started => start server
        if (!ServerProcess.isRunning() && !ServerSettings.useRemoteServer) {
            ServerProcess.start()
        }

        // Error handler that shows a dialog asking the user to start a local server or dismiss battle dialog
        onErrorDisposable = Client.onError.subscribe {
            val option = JOptionPane.showConfirmDialog(this,
                    ResourceBundles.MESSAGES.get("could_not_connect_to_server_start_local_question"),
                    ResourceBundles.MESSAGES.get("title_question"),
                    JOptionPane.YES_NO_OPTION)

            if (option == YES_OPTION) {
                ServerProcess.start()
                Client.connect(URI(ServerSettings.endpoint))
            } else {
                dispose() // dispose the dialog, when server is not available
            }
        }

        // Connect to the server. The error handler above is triggered if the connection cannot be established
        if (!Client.isConnected) {
            Client.connect(URI(ServerSettings.endpoint))
        }
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("battle_dialog")
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        BattleDialog.isVisible = true
    }
}