package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.menu.Menu
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.arena.ControlPanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol

object MainWindow : RcFrame("main_window") {

    init {
        RegisterWsProtocol

        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(900, 800)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(LogoPanel)

        jMenuBar = Menu

        ClientEvents.apply {
            onGameStarted.subscribe(MainWindow) { showBattle() }
            onGameEnded.subscribe(MainWindow) { showLogo() }
            onGameAborted.subscribe(MainWindow) { showLogo() }
        }

        onClosing { close() }
        Runtime.getRuntime().addShutdownHook(Thread { close() })
    }

    private fun close() {
        Client.close()
        BootProcess.stopRunning()
        ServerProcess.stop()
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
}