package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.booter.BootProcess
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.menu.Menu
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.ui.arena.ArenaPanel
import dev.robocode.tankroyale.gui.ui.arena.LogoPanel
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.control.ControlEventHandlers
import dev.robocode.tankroyale.gui.ui.control.ControlPanel
import dev.robocode.tankroyale.gui.ui.extensions.WindowExt.onClosing
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import java.awt.BorderLayout
import javax.swing.JPanel

object MainWindow : RcFrame("main_window") {

    init {
        defaultCloseOperation = EXIT_ON_CLOSE

        setSize(900, 800)
        setLocationRelativeTo(null) // center on screen

        contentPane.add(MainPanel)

        jMenuBar = Menu

        ClientEvents.apply {
            onGameStarted.subscribe(MainWindow) { MainPanel.showArena() }
            onGameEnded.subscribe(MainWindow) { MainPanel.showLogo() }
            onGameAborted.subscribe(MainWindow) { MainPanel.showLogo() }
        }
        ServerEvents.onStopped.subscribe(MainWindow) { MainPanel.showLogo() }

        onClosing { close() }
        Runtime.getRuntime().addShutdownHook(Thread { close() })
    }

    private fun close() {
        Client.close()
        BootProcess.stopRunning()
        ServerProcess.stop()
    }

    private object MainPanel : JPanel() {
        init {
            ControlEventHandlers

            layout = BorderLayout()
            add(LogoPanel, BorderLayout.CENTER)
            add(ControlPanel, BorderLayout.SOUTH)

            ControlPanel.isVisible = false
        }

        fun showLogo() {
            remove(ArenaPanel)
            add(LogoPanel, BorderLayout.CENTER)

            refresh()
        }

        fun showArena() {
            remove(LogoPanel)
            add(ArenaPanel, BorderLayout.CENTER)

            refresh()
        }

        private fun refresh() {
            ControlPanel.isVisible = true
            validate()
            repaint()
        }
    }
}