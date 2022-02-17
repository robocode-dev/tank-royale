package dev.robocode.tankroyale.gui

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.arena.ControlPanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.components.Images
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.config.BotRootDirectoriesConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.ui.newbattle.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import java.awt.EventQueue
import java.awt.Taskbar
import javax.swing.UIManager


object MainWindow : RcFrame("main_window"), AutoCloseable {

    init {
        RegisterWsProtocol

        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(900, 800)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(LogoPanel)

        jMenuBar = MainWindowMenu

        MainWindowMenu.apply {
            onStartBattle.invokeLater(MainWindow) { startBattle() }
            onSetupRules.invokeLater(MainWindow) { SetupRulesDialog.isVisible = true }
            onShowServerLog.invokeLater(MainWindow) { ServerLogWindow.isVisible = true }
            onServerConfig.invokeLater(MainWindow) { SelectServerDialog.isVisible = true }
            onBotDirConfig.invokeLater(MainWindow) { BotRootDirectoriesConfigDialog.isVisible = true }
        }

        Client.apply {
            onGameStarted.subscribe(MainWindow) { showBattle() }
            onGameEnded.subscribe(MainWindow) { showLogo() }
            onGameAborted.subscribe(MainWindow) { showLogo() }
        }

        onClosing {
            BootProcess.stopRunning()
            close()
        }
    }

    private fun startBattle() {
        Server.apply {
            onConnected.subscribe(MainWindow) { NewBattleDialog.isVisible = true }
            try {
                connectOrStart()
            } catch (e: Exception) {
                System.err.println(e.message)
            }
        }
    }

    private fun showLogo() {
        contentPane.apply {
            remove(ControlPanel)
            add(LogoPanel)
        }
        validate()
        repaint()
    }

    private fun showBattle() {
        contentPane.apply {
            remove(LogoPanel)
            add(ControlPanel)
        }
        validate()
        repaint()
    }

    override fun close() {
        Client.close()
        BootProcess.stopRunning()
        ServerProcess.stop()
    }
}

private fun main() {
    Runtime.getRuntime().addShutdownHook(Thread {
        MainWindow.close()
    })

    try {
        Taskbar.getTaskbar().iconImage = Images.tankImage // for macOS
    } catch (ignore: UnsupportedOperationException) {}

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}