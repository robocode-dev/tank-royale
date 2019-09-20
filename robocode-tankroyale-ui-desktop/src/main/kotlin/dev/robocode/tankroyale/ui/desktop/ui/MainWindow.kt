package dev.robocode.tankroyale.ui.desktop.ui

import kotlinx.serialization.ImplicitReflectionSerializer
import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.ui.battle.BattleDialog
import dev.robocode.tankroyale.ui.desktop.ui.battle.BattlePanel
import dev.robocode.tankroyale.ui.desktop.ui.battle.LogoPanel
import dev.robocode.tankroyale.ui.desktop.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.ui.desktop.ui.server.SelectServerDialog
import dev.robocode.tankroyale.ui.desktop.ui.server.ServerWindow
import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.UIManager

@ImplicitReflectionSerializer
object MainWindow : JFrame(getWindowTitle()), AutoCloseable {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(800, 600)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(LogoPanel)

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
        MainWindowMenu.onShowServerLog.invokeLater { ServerWindow.isVisible = true }
        MainWindowMenu.onServerConfig.invokeLater { SelectServerDialog.isVisible = true }
        MainWindowMenu.onBotDirConfig.invokeLater { BotDirectoryConfigDialog.isVisible = true }

        Client.onGameStarted.subscribe { showBattle() }
        Client.onGameEnded.subscribe { showLogo() }
        Client.onGameAborted.subscribe { showLogo() }

        onClosing {
            close()
        }
    }

    private fun showLogo() {
        contentPane.remove(BattlePanel)
        contentPane.add(LogoPanel)

        validate()
        repaint()
    }

    private fun showBattle() {
        contentPane.remove(LogoPanel)
        contentPane.add(BattlePanel)

        validate()
        repaint()
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