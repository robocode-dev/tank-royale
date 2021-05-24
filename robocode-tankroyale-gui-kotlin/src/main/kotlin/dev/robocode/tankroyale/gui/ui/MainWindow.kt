package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.bootstrap.BootstrapProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.arena.ControlPanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.config.BotDirectoryConfigDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.selection.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.server.*
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import java.awt.EventQueue
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.UIManager


object MainWindow : JFrame(ResourceBundles.UI_TITLES.get("main_window")), AutoCloseable {

    init {
        RegisterWsProtocol

        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(880, 760)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(LogoPanel)

        jMenuBar = MainWindowMenu

        val iconStream = javaClass.getResourceAsStream("/gfx/Tank.png")
        val iconImage = ImageIcon(ImageIO.read(iconStream))
        setIconImage(iconImage.image)

        MainWindowMenu.apply {
            onStartBattle.invokeLater(this) { startBattle() }
            onSetupRules.invokeLater(this) { SetupRulesDialog.isVisible = true }
            onShowServerLog.invokeLater(this) { ServerLogWindow.isVisible = true }
            onServerConfig.invokeLater(this) { SelectServerDialog.isVisible = true }
            onBotDirConfig.invokeLater(this) { BotDirectoryConfigDialog.isVisible = true }
        }

        Client.apply {
            onGameStarted.subscribe(this) { showBattle() }
            onGameEnded.subscribe(this) { showLogo() }
            onGameAborted.subscribe(this) { showLogo() }
        }

        onClosing {
            BootstrapProcess.stopRunning()
            close()
        }
    }

    private fun startBattle() {
        Server.apply {
            onConnected.subscribe(this) { NewBattleDialog.isVisible = true }
            connectOrStart()
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