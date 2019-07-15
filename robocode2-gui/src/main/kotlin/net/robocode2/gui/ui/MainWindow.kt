package net.robocode2.gui.ui

import kotlinx.serialization.ImplicitReflectionSerializer
import net.robocode2.gui.client.Client
import net.robocode2.gui.extensions.WindowExt.onClosing
import net.robocode2.gui.server.ServerProcess
import net.robocode2.gui.ui.server.ServerWindow
import net.robocode2.gui.ui.battle.ArenaPanel
import net.robocode2.gui.ui.battle.BattleDialog
import net.robocode2.gui.ui.server.ServerConfigDialog
import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

@ImplicitReflectionSerializer
object MainWindow : JFrame(getWindowTitle()), AutoCloseable {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(800, 600)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(ArenaPanel())

        jMenuBar = MainWindowMenu

        MainWindowMenu.onNewBattle.invokeLater {
            BattleDialog.apply {
                selectBotsTab()
                isVisible = true
            }
        }

        MainWindowMenu.onSetupRules.invokeLater {
            BattleDialog.apply {
                selectSetupRulesTab()
                isVisible = true
            }
        }

        MainWindowMenu.onShowServerLog.invokeLater {
            ServerWindow.isVisible = true
        }

        MainWindowMenu.onServerConfig.invokeLater {
            ServerConfigDialog.isVisible = true
        }

        onClosing {
            close()
        }
    }

    override fun close() {
        Client.close()
        ServerProcess.stop()
    }
}

private fun getWindowTitle(): String {
    return ResourceBundles.UI_TITLES.get("main_window")
}

@ImplicitReflectionSerializer
private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}