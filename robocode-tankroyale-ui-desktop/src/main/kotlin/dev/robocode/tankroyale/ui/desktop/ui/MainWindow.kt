package dev.robocode.tankroyale.ui.desktop.ui

import dev.robocode.tankroyale.ui.desktop.client.Client
import dev.robocode.tankroyale.ui.desktop.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.ui.desktop.server.ServerProcess
import dev.robocode.tankroyale.ui.desktop.ui.battle.BattleDialog
import dev.robocode.tankroyale.ui.desktop.ui.battle.BattlePanel
import dev.robocode.tankroyale.ui.desktop.ui.battle.LogoPanel
import dev.robocode.tankroyale.ui.desktop.ui.bootstrap.BootstrapDialog
import dev.robocode.tankroyale.ui.desktop.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.ui.desktop.ui.server.PrepareServerCommand
import dev.robocode.tankroyale.ui.desktop.ui.server.SelectServerDialog
import dev.robocode.tankroyale.ui.desktop.ui.server.ServerLogWindow
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import java.awt.EventQueue
import java.io.Closeable
import javax.swing.JFrame
import javax.swing.UIManager

@UnstableDefault
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

        MainWindowMenu.onNewBattle2.invokeLater {
            Client.onConnected.subscribe {
                BootstrapDialog.isVisible = true
            }
            PrepareServerCommand().execute()

            // TODO: Select bots to participate in battle and start the game
        }

        MainWindowMenu.onSetupRules.invokeLater {
            BattleDialog.apply {
                selectSetupRulesTab()
                isVisible = true
            }
        }
        MainWindowMenu.onShowServerLog.invokeLater { ServerLogWindow.isVisible = true }
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