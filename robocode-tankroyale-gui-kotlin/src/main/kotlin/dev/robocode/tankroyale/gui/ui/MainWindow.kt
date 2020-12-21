package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.arena.ControlPanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.selection.*
import dev.robocode.tankroyale.gui.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.server.PrepareServerCommand
import dev.robocode.tankroyale.gui.ui.server.SelectServerDialog
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow
import java.awt.EventQueue
import java.io.Closeable
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.UIManager

object MainWindow : JFrame(ResourceBundles.UI_TITLES.get("main_window")), AutoCloseable {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(880, 760)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(LogoPanel)

        jMenuBar = MainWindowMenu

        val iconUrl = javaClass.classLoader.getResource("gfx/Tank.png")
        val iconImage = ImageIcon(iconUrl)
        setIconImage(iconImage.image)

        MainWindowMenu.apply {
            onSelectBots.invokeLater { selectBots() }
            onSetupRules.invokeLater { SetupRulesDialog.isVisible = true }
            onShowServerLog.invokeLater { ServerLogWindow.isVisible = true }
            onServerConfig.invokeLater { SelectServerDialog.isVisible = true }
            onBotDirConfig.invokeLater { BotDirectoryConfigDialog.isVisible = true }
        }

        Client.apply {
            onGameStarted.subscribe { showBattle() }
//            onGameEnded.subscribe { showLogo() }
//            onGameAborted.subscribe { showLogo() }
        }

        onClosing {
            BootstrapProcess.stopRunning()
            close()
        }
    }

    private fun selectBots() {
        var disposable: Closeable? = null
        disposable = Client.onConnected.subscribe {
            NewBattleDialog.isVisible = true
            // Make sure to dispose. Otherwise the dialog will be shown when testing if the server is running
            disposable?.close()
        }
        PrepareServerCommand().execute()
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
        contentPane.remove(LogoPanel)
        contentPane.add(ControlPanel)

        validate()
        repaint()
    }

    override fun close() {
        Client.close()
        BootstrapProcess.stopRunning()
        ServerProcess.stop()
    }
}

private fun main() {
    Runtime.getRuntime().addShutdownHook(Thread {
        MainWindow.close()
    })

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    EventQueue.invokeLater {
        MainWindow.isVisible = true
    }
}