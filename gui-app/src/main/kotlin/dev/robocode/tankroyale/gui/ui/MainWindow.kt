package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.menu.Menu
import dev.robocode.tankroyale.gui.ui.menu.MenuEvents
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.arena.ControlPanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.ui.newbattle.NewBattleDialog
import dev.robocode.tankroyale.gui.ui.server.Server
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol


object MainWindow : RcFrame("main_window"), AutoCloseable {

    init {
        RegisterWsProtocol

        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(900, 800)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(LogoPanel)

        jMenuBar = Menu

        MenuEvents.onStartBattle.invokeLater(this) { startBattle() }

        ClientEvents.apply {
            onGameStarted.subscribe(this) { showBattle() }
            onGameEnded.subscribe(this) { showLogo() }
            onGameAborted.subscribe(this) { showLogo() }
        }

        onClosing {
            BootProcess.stopRunning()
            close()
        }
    }

    private fun startBattle() {
        ServerEvents.onConnected.subscribe(this) { NewBattleDialog.isVisible = true }
        try {
            Server.connectOrStart()
        } catch (e: Exception) {
            System.err.println(e.message)
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